package co.tiendasegura.fiados.application.dto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record RegistrarAbonoCommand(
        UUID tiendaId,
        UUID clienteId,
        BigDecimal monto,
        String metodoPago,
        String nota
) {
    public RegistrarAbonoCommand {
        Objects.requireNonNull(tiendaId, "El tiendaId es obligatorio");
        Objects.requireNonNull(clienteId, "El clienteId es obligatorio");
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del abono debe ser mayor a cero");
        }
    }
}
