package co.tiendasegura.ventas.application.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta HTTP con los datos de una venta y sus detalles.
 */
@Serdeable
public record VentaResponse(
        UUID id,
        UUID tiendaId,
        UUID cajeroId,
        UUID clienteId,
        Instant fecha,
        BigDecimal subtotal,
        BigDecimal descuento,
        BigDecimal total,
        String metodoPago,
        BigDecimal montoRecibido,
        BigDecimal cambio,
        String estado,
        List<DetalleVentaResponse> detalles
) {
    @Serdeable
    public record DetalleVentaResponse(
            UUID id,
            UUID productoId,
            String nombreProducto,
            int cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {}
}
