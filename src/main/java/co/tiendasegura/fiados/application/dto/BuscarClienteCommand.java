package co.tiendasegura.fiados.application.dto;

import java.util.Objects;
import java.util.UUID;

public record BuscarClienteCommand(
        UUID tiendaId,
        UUID id
) {
    public BuscarClienteCommand {
        Objects.requireNonNull(tiendaId, "El tiendaId es obligatorio");
        Objects.requireNonNull(id, "El ID del cliente es obligatorio");
    }
}
