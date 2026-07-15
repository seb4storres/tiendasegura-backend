package co.tiendasegura.auth.infrastructure.web.dto;

import co.tiendasegura.auth.application.dto.LoginCommand;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO HTTP para el endpoint POST /auth/login.
 */
@Serdeable
public record LoginRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
    public LoginCommand toCommand() {
        return new LoginCommand(email, password);
    }
}