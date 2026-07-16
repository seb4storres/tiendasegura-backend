package co.tiendasegura.fiados.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CrearClienteCommand(
        UUID tiendaId,
        String nombre,
        String telefono,
        String cedula,
        String direccion,
        BigDecimal limiteCredito
) {
    public CrearClienteCommand {
        if (tiendaId == null) {
            throw new IllegalArgumentException("El tiendaId es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        nombre = nombre.strip();
        limiteCredito = limiteCredito != null ? limiteCredito : BigDecimal.ZERO;
    }
}
