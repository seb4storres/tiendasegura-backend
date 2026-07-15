package co.tiendasegura.auth.application.services;

import co.tiendasegura.auth.application.dto.AuthResponse;
import co.tiendasegura.auth.application.dto.LoginCommand;
import co.tiendasegura.auth.application.mapper.AuthMapper;
import co.tiendasegura.auth.application.ports.in.LoginUseCase;
import co.tiendasegura.auth.domain.exceptions.CredencialesInvalidasException;
import co.tiendasegura.auth.domain.exceptions.TiendaInactivaException;
import co.tiendasegura.auth.domain.exceptions.UsuarioInactivoException;
import co.tiendasegura.auth.domain.model.Tienda;
import co.tiendasegura.auth.domain.model.Usuario;
import co.tiendasegura.auth.domain.ports.out.PasswordEncoderPort;
import co.tiendasegura.auth.domain.ports.out.TiendaRepositoryPort;
import co.tiendasegura.auth.domain.ports.out.TokenProviderPort;
import co.tiendasegura.auth.domain.ports.out.UsuarioRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caso de uso: autenticar un usuario existente.
 *
 * Flujo:
 * 1. Buscar usuario por email (mensaje genérico si no existe — seguridad).
 * 2. Verificar password contra el hash almacenado.
 * 3. Verificar que el usuario esté activo.
 * 4. Cargar la tienda y verificar que esté activa.
 * 5. Generar tokens JWT.
 * 6. Retornar AuthResponse.
 */
public class LoginService implements LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private final UsuarioRepositoryPort usuarioRepository;
    private final TiendaRepositoryPort tiendaRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;

    public LoginService(UsuarioRepositoryPort usuarioRepository,
                        TiendaRepositoryPort tiendaRepository,
                        PasswordEncoderPort passwordEncoder,
                        TokenProviderPort tokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.tiendaRepository = tiendaRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResponse ejecutar(LoginCommand command) {
        log.debug("Intento de login para: {}", command.email());

        // 1. Buscar usuario — mensaje genérico si no existe (no revelar si el email está registrado)
        Usuario usuario = usuarioRepository.buscarPorEmail(command.email())
                .orElseThrow(CredencialesInvalidasException::new);

        // 2. Verificar password
        if (!passwordEncoder.verificar(command.password(), usuario.getPasswordHash())) {
            log.warn("Password incorrecto para: {}", command.email());
            throw new CredencialesInvalidasException();
        }

        // 3. Verificar que el usuario esté activo
        if (!usuario.isActivo()) {
            log.warn("Intento de login con cuenta inactiva: {}", command.email());
            throw new UsuarioInactivoException(command.email());
        }

        // 4. Cargar y verificar tienda
        Tienda tienda = tiendaRepository.buscarPorId(usuario.getTiendaId())
                .orElseThrow(() -> {
                    log.error("Tienda no encontrada para usuario: {} (tiendaId: {})",
                            command.email(), usuario.getTiendaId());
                    return new CredencialesInvalidasException();
                });

        if (!tienda.isActiva()) {
            log.warn("Login rechazado — tienda inactiva: {} (ID: {})",
                    tienda.getNombre(), tienda.getId());
            throw new TiendaInactivaException(tienda.getId());
        }

        // 5. Generar tokens
        String accessToken = tokenProvider.generarAccessToken(usuario);
        String refreshToken = tokenProvider.generarRefreshToken(usuario);

        log.info("Login exitoso: {} (tienda: {})", usuario.getEmail(), tienda.getNombre());

        // 6. Mapear y retornar
        return AuthMapper.toAuthResponse(usuario, tienda, accessToken, refreshToken);
    }
}