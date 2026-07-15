package co.tiendasegura.auth.application.ports.in;

import co.tiendasegura.auth.application.dto.AuthResponse;
import co.tiendasegura.auth.application.dto.LoginCommand;

/**
 * Puerto de entrada: autenticar un usuario existente.
 * Valida credenciales, verifica que la tienda esté activa
 * y retorna un JWT para la sesión.
 */
public interface LoginUseCase {

    AuthResponse ejecutar(LoginCommand command);
}