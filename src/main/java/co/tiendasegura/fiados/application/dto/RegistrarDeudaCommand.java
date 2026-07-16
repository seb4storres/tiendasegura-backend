package co.tiendasegura.fiados.application.dto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Command para registrar una deuda contra el saldo de un cliente.
 * Es el punto de entrada que usa el dominio de Ventas (a través de
 * FiadosPort/FiadosPortAdapter) cuando una venta se paga con FIADO.
 * ventaId no se persiste en este modelo simple — viaja solo para
 * trazabilidad en logs.
 */
public record RegistrarDeudaCommand(
        UUID tiendaId,
        UUID clienteId,
        UUID ventaId,
        BigDecimal monto
) {
    public RegistrarDeudaCommand {
        Objects.requireNonNull(tiendaId, "El tiendaId es obligatorio");
        Objects.requireNonNull(clienteId, "El clienteId es obligatorio");
        Objects.requireNonNull(ventaId, "El ventaId es obligatorio");
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de la deuda debe ser mayor a cero");
        }
    }
}
