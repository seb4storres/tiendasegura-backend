package co.tiendasegura.auth.domain.ports.out;

import co.tiendasegura.auth.domain.model.Usuario;

/**
 * Puerto de salida para generación de tokens de autenticación.
 * La implementación concreta usará Micronaut Security JWT,
 * pero el dominio/aplicación solo conoce esta interfaz.
 */
public interface TokenProviderPort {

    /**
     * Genera un access token JWT con los claims del usuario:
     * sub=email, tiendaId, userId, rol.
     */
    String generarAccessToken(Usuario usuario);

    /**
     * Genera un refresh token para renovar la sesión.
     */
    String generarRefreshToken(Usuario usuario);
}