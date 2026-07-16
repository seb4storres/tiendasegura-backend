package co.tiendasegura.ventas.infrastructure.web.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

@Serdeable
public record ItemVentaRequest(
        @NotNull(message = "El productoId del ítem es obligatorio")
        UUID productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        Integer cantidad
) {}
