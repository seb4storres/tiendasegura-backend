package co.tiendasegura.auth.application.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.UUID;

/**
 * Respuesta de autenticación (login o registro exitoso).
 * Contiene todo lo que el frontend necesita para iniciar sesión:
 * tokens + datos básicos del usuario y su tienda.
 */
@Serdeable
public record AuthResponse(
        String accessToken,
        String refreshToken,
        UsuarioInfo usuario,
        TiendaInfo tienda
) {
    /**
     * Datos mínimos del usuario autenticado — sin exponer el passwordHash.
     */
    @Serdeable
    public record UsuarioInfo(
            UUID id,
            String email,
            String nombre,
            String rol
    ) {}

    /**
     * Datos mínimos de la tienda — el frontend los usa para mostrar
     * el nombre en el header y enviar el tiendaId en las peticiones.
     */
    @Serdeable
    public record TiendaInfo(
            UUID id,
            String nombre,
            String plan
    ) {}
}