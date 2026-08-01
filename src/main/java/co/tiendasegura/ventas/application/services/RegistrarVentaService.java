package co.tiendasegura.ventas.application.services;

import co.tiendasegura.ventas.application.dto.ItemVentaCommand;
import co.tiendasegura.ventas.application.dto.RegistrarVentaCommand;
import co.tiendasegura.ventas.application.dto.VentaResponse;
import co.tiendasegura.ventas.application.mapper.VentaMapper;
import co.tiendasegura.ventas.application.ports.in.RegistrarVentaUseCase;
import co.tiendasegura.ventas.domain.exceptions.ProductoNoDisponibleException;
import co.tiendasegura.ventas.domain.exceptions.StockInsuficienteException;
import co.tiendasegura.ventas.domain.exceptions.VentaDuplicadaException;
import co.tiendasegura.ventas.domain.model.DetalleVenta;
import co.tiendasegura.ventas.domain.model.MetodoPago;
import co.tiendasegura.ventas.domain.model.Venta;
import co.tiendasegura.ventas.domain.ports.out.FiadosPort;
import co.tiendasegura.ventas.domain.ports.out.InventarioPort;
import co.tiendasegura.ventas.domain.ports.out.VentaRepositoryPort;
import io.micronaut.transaction.TransactionOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Caso de uso: registrar una venta (motor transaccional del POS).
 *
 * Flujo:
 * 1. Idempotencia (camino rápido): si ya existe una venta con este ID en la
 *    tienda, se retorna tal cual — sin reprocesar, sin volver a tocar stock.
 *    Cubre el caso común de reintento por red intermitente del frontend
 *    offline-first, cuando el intento anterior ya comprometió su transacción.
 * 2. Por cada ítem: leer el producto actual vía InventarioPort, validar
 *    que esté activo y tenga stock suficiente, construir el DetalleVenta
 *    con el precio como snapshot inmutable, y descontar el stock
 *    respetando la versión leída (optimistic concurrency control).
 * 3. Construir la Venta — el dominio calcula subtotal/total/cambio/iva.
 * 4. Si el método de pago es FIADO, registrar la deuda contra el cliente
 *    vía FiadosPort — si el cliente está bloqueado o el crédito no
 *    alcanza, la excepción revierte TODA la venta (stock incluido).
 * 5. Persistir venta + detalles.
 *
 * Idempotencia (camino de carrera): los pasos 2-5 corren dentro de una
 * transacción explícita (transactionOperations.executeWrite), NO de un
 * @Transactional declarativo que envuelva todo ejecutar(). Esto es a
 * propósito: si dos reintentos del Service Worker llegan casi al mismo
 * tiempo, ambos pueden pasar el chequeo del paso 1 antes de que cualquiera
 * confirme. El perdedor choca contra la PK de `ventas` dentro de
 * VentaJdbcAdapter#guardar y lanza VentaDuplicadaException — eso hace que
 * SOLO esa transacción interna se revierta (incluyendo el stock que ese
 * intento ya había descontado). Como el catch vive FUERA de esa transacción,
 * es seguro volver a leer la venta (ya comprometida por el intento ganador)
 * y devolverla como éxito, sin duplicar el descuento de stock ni propagar
 * un error al cliente.
 */
public class RegistrarVentaService implements RegistrarVentaUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarVentaService.class);

    private final VentaRepositoryPort ventaRepository;
    private final InventarioPort inventarioPort;
    private final FiadosPort fiadosPort;
    private final TransactionOperations<Connection> transactionOperations;

    public RegistrarVentaService(VentaRepositoryPort ventaRepository, InventarioPort inventarioPort,
                                 FiadosPort fiadosPort, TransactionOperations<Connection> transactionOperations) {
        this.ventaRepository = ventaRepository;
        this.inventarioPort = inventarioPort;
        this.fiadosPort = fiadosPort;
        this.transactionOperations = transactionOperations;
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

        try {
            Venta venta = transactionOperations.executeWrite(status -> procesarVenta(command));
            log.info("Venta {} registrada exitosamente. Total: {}", venta.getId(), venta.getTotal());
            return VentaMapper.toResponse(venta);

        } catch (VentaDuplicadaException e) {
            // Carrera perdida: la transacción de ESTE intento ya se revirtió
            // por completo (stock incluido). El intento que ganó ya está
            // comprometido y visible — se retorna como éxito idempotente en
            // vez de propagar el error, para que el reintento del cliente
            // sea indistinguible de una creación original exitosa.
            log.info("Venta {} — colisión de idempotencia concurrente, retornando el resultado ya persistido",
                    command.id());
            return ventaRepository.buscarPorId(command.tiendaId(), command.id())
                    .map(VentaMapper::toResponse)
                    .orElseThrow(() -> e);
        }
    }

    private Venta procesarVenta(RegistrarVentaCommand command) {
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

        return ventaRepository.guardar(venta);
    }
}
