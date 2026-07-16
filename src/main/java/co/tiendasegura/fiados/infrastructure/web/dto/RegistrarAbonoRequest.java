package co.tiendasegura.fiados.infrastructure.web.dto;

import co.tiendasegura.fiados.application.dto.RegistrarAbonoCommand;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

@Serdeable
public record RegistrarAbonoRequest(
        @NotNull(message = "El clienteId es obligatorio")
        UUID clienteId,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a cero")
        BigDecimal monto,

        String metodoPago,

        String nota
) {
    public RegistrarAbonoCommand toCommand(UUID tiendaId) {
        return new RegistrarAbonoCommand(tiendaId, clienteId, monto, metodoPago, nota);
    }
}
