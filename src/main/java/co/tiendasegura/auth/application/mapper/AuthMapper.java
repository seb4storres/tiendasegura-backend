package co.tiendasegura.auth.application.mapper;

import co.tiendasegura.auth.application.dto.AuthResponse;
import co.tiendasegura.auth.domain.model.Tienda;
import co.tiendasegura.auth.domain.model.Usuario;

/**
 * Mapper manual (sin MapStruct ni ModelMapper) para transformaciones
 * entre entidades de dominio y DTOs de respuesta.
 * Clase utilitaria con métodos estáticos — no necesita estado.
 */
public final class AuthMapper {

    private AuthMapper() {
        // No instanciable
    }

    /**
     * Construye el AuthResponse completo a partir de las entidades
     * de dominio y los tokens generados.
     */
    public static AuthResponse toAuthResponse(Usuario usuario, Tienda tienda,
                                              String accessToken, String refreshToken) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                toUsuarioInfo(usuario),
                toTiendaInfo(tienda)
        );
    }

    public static AuthResponse.UsuarioInfo toUsuarioInfo(Usuario usuario) {
        return new AuthResponse.UsuarioInfo(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol().name()
        );
    }

    public static AuthResponse.TiendaInfo toTiendaInfo(Tienda tienda) {
        return new AuthResponse.TiendaInfo(
                tienda.getId(),
                tienda.getNombre(),
                tienda.getPlan().name()
        );
    }
}