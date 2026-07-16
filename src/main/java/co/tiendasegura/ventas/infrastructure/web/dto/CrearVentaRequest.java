package co.tiendasegura.ventas.infrastructure.web.dto;

import co.tiendasegura.ventas.application.dto.ItemVentaCommand;
import co.tiendasegura.ventas.application.dto.RegistrarVentaCommand;
import co.tiendasegura.ventas.domain.model.MetodoPago;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO HTTP para el endpoint POST /ventas.
 *
 * `id` es la clave de idempotencia: la genera el cliente (frontend
 * offline-first) ANTES de intentar la venta, y se reenvía tal cual en
 * cada reintento de red. tiendaId/cajeroId NO viajan en el body — salen
 * del tenant context (JWT), nunca del cliente.
 */
@Serdeable
public record CrearVentaRequest(
        @NotNull(message = "El ID de la venta (clave de idempotencia) es obligatorio")
        UUID id,

        UUID clienteId,

        @NotEmpty(message = "La venta debe tener al menos un ítem")
        @Valid
        List<ItemVentaRequest> items,

        @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
        BigDecimal descuento,

        @NotNull(message = "El método de pago es obligatorio")
        MetodoPago metodoPago,

        BigDecimal montoRecibido,

        String notas
) {
    public RegistrarVentaCommand toCommand(UUID tiendaId, UUID cajeroId) {
        List<ItemVentaCommand> itemsCommand = items.stream()
                .map(i -> new ItemVentaCommand(i.productoId(), i.cantidad()))
                .toList();

        return new RegistrarVentaCommand(
                id, tiendaId, cajeroId, clienteId,
                itemsCommand, descuento, metodoPago, montoRecibido, notas
        );
    }
}
