package co.tiendasegura.fiados.infrastructure.web.dto;

import co.tiendasegura.fiados.application.dto.CrearClienteCommand;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO HTTP para el endpoint POST /clientes.
 * tiendaId NO viaja en el body — sale del tenant context (JWT).
 */
@Serdeable
public record CrearClienteRequest(
        @NotBlank(message = "El nombre del cliente es obligatorio")
        @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
        String nombre,

        String telefono,

        String cedula,

        String direccion,

        @DecimalMin(value = "0.0", message = "El límite de crédito no puede ser negativo")
        BigDecimal limiteCredito
) {
    public CrearClienteCommand toCommand(UUID tiendaId) {
        return new CrearClienteCommand(tiendaId, nombre, telefono, cedula, direccion, limiteCredito);
    }
}
