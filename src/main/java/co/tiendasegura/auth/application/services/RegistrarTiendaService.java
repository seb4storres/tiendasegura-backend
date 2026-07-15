package co.tiendasegura.auth.application.services;

import co.tiendasegura.auth.application.dto.AuthResponse;
import co.tiendasegura.auth.application.dto.RegistrarTiendaCommand;
import co.tiendasegura.auth.application.mapper.AuthMapper;
import co.tiendasegura.auth.application.ports.in.RegistrarTiendaUseCase;
import co.tiendasegura.auth.domain.exceptions.EmailYaRegistradoException;
import co.tiendasegura.auth.domain.model.Tienda;
import co.tiendasegura.auth.domain.model.Usuario;
import co.tiendasegura.auth.domain.ports.out.PasswordEncoderPort;
import co.tiendasegura.auth.domain.ports.out.TiendaRepositoryPort;
import co.tiendasegura.auth.domain.ports.out.TokenProviderPort;
import co.tiendasegura.auth.domain.ports.out.UsuarioRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caso de uso: registrar una tienda nueva con su administrador.
 *
 * Flujo:
 * 1. Verificar que el email no exista.
 * 2. Crear entidad Tienda (con UUID generado internamente).
 * 3. Persistir la tienda.
 * 4. Hashear el password via PasswordEncoderPort.
 * 5. Crear entidad Usuario con rol ADMIN.
 * 6. Persistir el usuario.
 * 7. Generar tokens JWT.
 * 8. Retornar AuthResponse.
 *
 * La anotación @Singleton y @Transactional se aplican en la configuración
 * de infraestructura, no aquí. Este servicio es Java puro + SLF4J.
 */
public class RegistrarTiendaService implements RegistrarTiendaUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarTiendaService.class);

    private final UsuarioRepositoryPort usuarioRepository;
    private final TiendaRepositoryPort tiendaRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;

    public RegistrarTiendaService(UsuarioRepositoryPort usuarioRepository,
                                  TiendaRepositoryPort tiendaRepository,
                                  PasswordEncoderPort passwordEncoder,
                                  TokenProviderPort tokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.tiendaRepository = tiendaRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResponse ejecutar(RegistrarTiendaCommand command) {
        log.info("Registrando tienda '{}' con admin '{}'",
                command.nombreTienda(), command.emailAdmin());

        // 1. Verificar unicidad del email
        if (usuarioRepository.existeEmail(command.emailAdmin())) {
            throw new EmailYaRegistradoException(command.emailAdmin());
        }

        // 2. Crear y persistir la tienda
        Tienda tienda = Tienda.crear(
                command.nombreTienda(),
                command.nit(),
                command.direccion(),
                command.telefono()
        );
        tienda = tiendaRepository.guardar(tienda);
        log.debug("Tienda creada con ID: {}", tienda.getId());

        // 3. Hashear password y crear usuario admin
        String passwordHash = passwordEncoder.codificar(command.passwordAdmin());

        Usuario admin = Usuario.crearAdmin(
                tienda.getId(),
                command.emailAdmin(),
                passwordHash,
                command.nombreAdmin()
        );
        admin = usuarioRepository.guardar(admin);
        log.debug("Usuario admin creado con ID: {}", admin.getId());

        // 4. Generar tokens
        String accessToken = tokenProvider.generarAccessToken(admin);
        String refreshToken = tokenProvider.generarRefreshToken(admin);

        log.info("Registro exitoso para tienda '{}' (ID: {})", tienda.getNombre(), tienda.getId());

        // 5. Mapear y retornar
        return AuthMapper.toAuthResponse(admin, tienda, accessToken, refreshToken);
    }
}