package co.tiendasegura.auth.application.ports.in;

import co.tiendasegura.auth.application.dto.AuthResponse;
import co.tiendasegura.auth.application.dto.RegistrarTiendaCommand;

/**
 * Puerto de entrada: registrar una tienda nueva con su usuario administrador.
 * Retorna un AuthResponse con el JWT para que el admin pueda comenzar
 * a operar inmediatamente después del registro.
 */
public interface RegistrarTiendaUseCase {

    AuthResponse ejecutar(RegistrarTiendaCommand command);
}