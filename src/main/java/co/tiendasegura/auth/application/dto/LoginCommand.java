package co.tiendasegura.auth.application.dto;

/**
 * Command inmutable para autenticación.
 */
public record LoginCommand(
        String email,
        String password
) {
    public LoginCommand {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("El password es obligatorio");
        }
        email = email.strip().toLowerCase();
    }
}