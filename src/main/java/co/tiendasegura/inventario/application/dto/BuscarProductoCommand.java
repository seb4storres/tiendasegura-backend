package co.tiendasegura.inventario.application.dto;

import java.util.Objects;
import java.util.UUID;

/**
 * Command inmutable para buscar un producto por ID, scoped a una tienda.
 */
public record BuscarProductoCommand(
        UUID tiendaId,
        UUID id
) {
    public BuscarProductoCommand {
        Objects.requireNonNull(tiendaId, "El tiendaId es obligatorio");
        Objects.requireNonNull(id, "El ID del producto es obligatorio");
    }
}
