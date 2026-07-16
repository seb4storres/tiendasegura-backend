package co.tiendasegura.ventas.application.dto;

import java.util.UUID;

public record ItemVentaCommand(
        UUID productoId,
        int cantidad
) {
    public ItemVentaCommand {
        if (productoId == null) {
            throw new IllegalArgumentException("El productoId del ítem es obligatorio");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
    }
}
