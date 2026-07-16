package co.tiendasegura.ventas.application.services;

import co.tiendasegura.ventas.application.dto.ItemVentaCommand;
import co.tiendasegura.ventas.application.dto.RegistrarVentaCommand;
import co.tiendasegura.ventas.application.dto.VentaResponse;
import co.tiendasegura.ventas.application.mapper.VentaMapper;
import co.tiendasegura.ventas.application.ports.in.RegistrarVentaUseCase;
import co.tiendasegura.ventas.domain.exceptions.ProductoNoDisponibleException;
import co.tiendasegura.ventas.domain.exceptions.StockInsuficienteException;
import co.tiendasegura.ventas.domain.model.DetalleVenta;
import co.tiendasegura.ventas.domain.model.MetodoPago;
import co.tiendasegura.ventas.domain.model.Venta;
import co.tiendasegura.ventas.domain.ports.out.FiadosPort;
import co.tiendasegura.ventas.domain.ports.out.InventarioPort;
import co.tiendasegura.ventas.domain.ports.out.VentaRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Caso de uso: registrar una venta (motor transaccional del POS).
 *
 * Flujo:
 * 1. Idempotencia: si ya existe una venta con este ID en la tienda, se
 *    retorna tal cual — sin reprocesar, sin volver a tocar stock. Cubre
 *    el caso común de reintento por red intermitente.
 * 2. Por cada ítem: leer el producto actual vía InventarioPort, validar
 *    que esté activo y tenga stock suficiente, construir el DetalleVenta
 *    con el precio como snapshot inmutable, y descontar el stock
 *    respetando la versión leída (optimistic concurrency control).
 * 3. Construir la Venta — el dominio calcula subtotal/total/cambio.
 * 4. Si el método de pago es FIADO, registrar la deuda contra el cliente
 *    vía FiadosPort — si el cliente está bloqueado o el crédito no
 *    alcanza, la excepción revierte TODA la venta (stock incluido).
 * 5. Persistir venta + detalles.
 *
 * Todo el método corre dentro de una única transacción (ver
 * VentasUseCaseFactory): si cualquier paso falla, se revierte todo,
 * incluyendo los descuentos de stock ya aplicados en este intento.
 */
public class RegistrarVentaService implements RegistrarVentaUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarVentaService.class);

    private final VentaRepositoryPort ventaRepository;
    private final InventarioPort inventarioPort;
    private final FiadosPort fiadosPort;

    public RegistrarVentaService(VentaRepositoryPort ventaRepository, InventarioPort inventarioPort,
                                 FiadosPort fiadosPort) {
        this.ventaRepository = ventaRepository;
        this.inventarioPort = inventarioPort;
        this.fiadosPort = fiadosPort;
    }

    @Override
    public VentaResponse ejecutar(RegistrarVentaCommand command) {
        Optional<Venta> existente = ventaRepository.buscarPorId(command.tiendaId(), command.id());
        if (existente.isPresent()) {
            log.info("Venta {} ya existía — retornando resultado idempotente", command.id());
            return VentaMapper.toResponse(existente.get());
        }

        log.info("Registrando venta {} para tienda {} (cajero {})",
                command.id(), command.tiendaId(), command.cajeroId());

        List<DetalleVenta> detalles = new ArrayList<>();
        for (ItemVentaCommand item : command.items()) {
            InventarioPort.ProductoDisponible producto = inventarioPort
                    .buscarProducto(command.tiendaId(), item.productoId())
                    .orElseThrow(() -> new ProductoNoDisponibleException(item.productoId()));

            if (!producto.activo()) {
                throw new ProductoNoDisponibleException(item.productoId());
            }
            if (producto.stock() < item.cantidad()) {
                throw new StockInsuficienteException(item.productoId(), item.cantidad(), producto.stock());
            }

            detalles.add(DetalleVenta.crear(
                    command.id(), item.productoId(), producto.nombre(),
                    item.cantidad(), producto.precioVenta()
            ));

            boolean descontado = inventarioPort.descontarStock(
                    command.tiendaId(), item.productoId(), item.cantidad(), producto.version());
            if (!descontado) {
                // Alguien más vendió/modificó el producto entre la lectura y este punto.
                throw new StockInsuficienteException(item.productoId(), item.cantidad(), producto.stock());
            }
        }

        Venta venta = Venta.crear(
                command.id(), command.tiendaId(), command.cajeroId(), command.clienteId(),
                detalles, command.descuento(), command.metodoPago(),
                command.montoRecibido(), command.notas()
        );

        if (venta.getMetodoPago() == MetodoPago.FIADO) {
            fiadosPort.registrarDeuda(command.tiendaId(), command.clienteId(), venta.getId(), venta.getTotal());
        }

        venta = ventaRepository.guardar(venta);

        log.info("Venta {} registrada exitosamente. Total: {}", venta.getId(), venta.getTotal());

        return VentaMapper.toResponse(venta);
    }
}
