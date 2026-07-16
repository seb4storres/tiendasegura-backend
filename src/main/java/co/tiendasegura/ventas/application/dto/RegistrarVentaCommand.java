package co.tiendasegura.ventas.application.dto;

import co.tiendasegura.ventas.domain.model.MetodoPago;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Command inmutable para registrar una venta.
 *
 * El id es la CLAVE DE IDEMPOTENCIA: la genera el cliente (frontend
 * offline-first) antes de intentar la venta, y la reenvía tal cual en cada
 * reintento de red. El caso de uso la usa para detectar reintentos
 * duplicados y evitar cobros dobles.
 *
 * tiendaId y cajeroId NO vienen del cliente — los resuelve el controller
 * a partir del tenant context (JWT), igual que en inventario.
 */
public record RegistrarVentaCommand(
        UUID id,
        UUID tiendaId,
        UUID cajeroId,
        UUID clienteId,
        List<ItemVentaCommand> items,
        BigDecimal descuento,
        MetodoPago metodoPago,
        BigDecimal montoRecibido,
        String notas
) {
    public RegistrarVentaCommand {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la venta (clave de idempotencia) es obligatorio");
        }
        if (tiendaId == null) {
            throw new IllegalArgumentException("El tiendaId es obligatorio");
        }
        if (cajeroId == null) {
            throw new IllegalArgumentException("El cajeroId es obligatorio");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un ítem");
        }
        if (metodoPago == null) {
            throw new IllegalArgumentException("El método de pago es obligatorio");
        }
        if (metodoPago == MetodoPago.FIADO && clienteId == null) {
            throw new IllegalArgumentException("Una venta a crédito (FIADO) requiere un cliente");
        }
        if (metodoPago == MetodoPago.EFECTIVO && montoRecibido == null) {
            throw new IllegalArgumentException("El monto recibido es obligatorio para pagos en efectivo");
        }
        descuento = descuento != null ? descuento : BigDecimal.ZERO;
        items = List.copyOf(items);
    }
}
