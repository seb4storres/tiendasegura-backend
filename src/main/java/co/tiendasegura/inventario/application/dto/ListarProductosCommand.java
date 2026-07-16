package co.tiendasegura.inventario.application.dto;

import java.util.Objects;
import java.util.UUID;

/**
 * Command inmutable para listar los productos de una tienda.
 */
public record ListarProductosCommand(
        UUID tiendaId
) {
    public ListarProductosCommand {
        Objects.requireNonNull(tiendaId, "El tiendaId es obligatorio");
    }
}
