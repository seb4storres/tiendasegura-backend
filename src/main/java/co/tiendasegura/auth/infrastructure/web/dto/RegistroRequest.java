package co.tiendasegura.auth.infrastructure.web.dto;

import co.tiendasegura.auth.application.dto.RegistrarTiendaCommand;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO HTTP para el endpoint POST /auth/registro.
 * Las validaciones de formato viven aquí (infraestructura),
 * las reglas de negocio en el servicio de aplicación.
 */
@Serdeable
public record RegistroRequest(
        @NotBlank(message = "El nombre de la tienda es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombreTienda,

        @Size(max = 20, message = "El NIT no puede superar 20 caracteres")
        String nit,

        String direccion,

        String telefono,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String emailAdmin,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
        String passwordAdmin,

        @NotBlank(message = "El nombre del administrador es obligatorio")
        String nombreAdmin
) {
    /**
     * Convierte este DTO HTTP al Command de la capa de aplicación.
     * El controller llama este método para cruzar la frontera
     * infraestructura → aplicación.
     */
    public RegistrarTiendaCommand toCommand() {
        return new RegistrarTiendaCommand(
                nombreTienda, nit, direccion, telefono,
                emailAdmin, passwordAdmin, nombreAdmin
        );
    }
}