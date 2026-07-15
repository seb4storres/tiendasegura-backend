package co.tiendasegura.auth.infrastructure.config;

import co.tiendasegura.auth.application.ports.in.LoginUseCase;
import co.tiendasegura.auth.application.ports.in.RegistrarTiendaUseCase;
import co.tiendasegura.auth.application.services.LoginService;
import co.tiendasegura.auth.application.services.RegistrarTiendaService;
import co.tiendasegura.auth.domain.ports.out.PasswordEncoderPort;
import co.tiendasegura.auth.domain.ports.out.TiendaRepositoryPort;
import co.tiendasegura.auth.domain.ports.out.TokenProviderPort;
import co.tiendasegura.auth.domain.ports.out.UsuarioRepositoryPort;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

/**
 * Factory de Micronaut que ensambla los casos de uso del dominio de auth.
 *
 * Aquí es donde el framework "toca" la capa de aplicación:
 *   - Los servicios se exponen como beans (@Singleton)
 *   - Se envuelven con @Transactional (la transacción abarca todo el caso de uso)
 *   - Las interfaces de los puertos de salida se resuelven por inyección
 *
 * Los servicios mismos NO tienen anotaciones de Micronaut.
 * Esta separación permite testear la lógica de negocio con mocks puros.
 */
@Factory
public class AuthUseCaseFactory {

    /**
     * Caso de uso: Registrar tienda con admin.
     * @Transactional garantiza que tienda + usuario se crean atómicamente.
     */
    @Singleton
    @Transactional
    public RegistrarTiendaUseCase registrarTiendaUseCase(
            UsuarioRepositoryPort usuarioRepository,
            TiendaRepositoryPort tiendaRepository,
            PasswordEncoderPort passwordEncoder,
            TokenProviderPort tokenProvider) {

        return new RegistrarTiendaService(
                usuarioRepository,
                tiendaRepository,
                passwordEncoder,
                tokenProvider
        );
    }

    /**
     * Caso de uso: Login de usuario existente.
     * @Transactional en modo lectura — busca usuario y tienda.
     */
    @Singleton
    @Transactional
    public LoginUseCase loginUseCase(
            UsuarioRepositoryPort usuarioRepository,
            TiendaRepositoryPort tiendaRepository,
            PasswordEncoderPort passwordEncoder,
            TokenProviderPort tokenProvider) {

        return new LoginService(
                usuarioRepository,
                tiendaRepository,
                passwordEncoder,
                tokenProvider
        );
    }
}