package co.tiendasegura.auth.infrastructure.persistence;

import co.tiendasegura.auth.domain.model.Rol;
import co.tiendasegura.auth.domain.model.Usuario;
import co.tiendasegura.auth.domain.ports.out.UsuarioRepositoryPort;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de salida: implementación JDBC del repositorio de Usuarios.
 *
 * NO aplica tenant context (SET LOCAL) porque los métodos de auth necesitan
 * acceso cross-tenant:
 *   - buscarPorEmail: el email es único global (un email → una sola cuenta SaaS).
 *   - existeEmail: verificar duplicados en registro, sin saber el tenant aún.
 *   - guardar: insertar el admin durante registro de tienda nueva.
 *
 * Para operaciones tenant-scoped (listar usuarios de una tienda, etc.),
 * se creará un adapter separado en el dominio de gestión de usuarios
 * que SÍ usará TenantConnectionHelper.
 */
@Singleton
public class UsuarioJdbcAdapter implements UsuarioRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(UsuarioJdbcAdapter.class);

    private final DataSource dataSource;

    public UsuarioJdbcAdapter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        String sql = """
                INSERT INTO usuarios (id, tienda_id, email, password_hash, nombre, rol, activo, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    email = EXCLUDED.email,
                    password_hash = EXCLUDED.password_hash,
                    nombre = EXCLUDED.nombre,
                    rol = EXCLUDED.rol,
                    activo = EXCLUDED.activo,
                    updated_at = EXCLUDED.updated_at
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, usuario.getId());
            ps.setObject(2, usuario.getTiendaId());
            ps.setString(3, usuario.getEmail());
            ps.setString(4, usuario.getPasswordHash());
            ps.setString(5, usuario.getNombre());
            ps.setString(6, usuario.getRol().name());
            ps.setBoolean(7, usuario.isActivo());
            ps.setTimestamp(8, Timestamp.from(usuario.getCreatedAt()));
            ps.setTimestamp(9, Timestamp.from(usuario.getUpdatedAt()));

            ps.executeUpdate();
            log.debug("Usuario guardado: {} ({})", usuario.getEmail(), usuario.getId());
            return usuario;

        } catch (SQLException e) {
            // Detectar violación de unique constraint en email
            if (e.getSQLState() != null && e.getSQLState().equals("23505")) {
                throw new RuntimeException("Email duplicado: " + usuario.getEmail(), e);
            }
            throw new RuntimeException("Error guardando usuario: " + usuario.getId(), e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.toLowerCase().strip());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando usuario por email: " + email, e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando usuario por ID: " + id, e);
        }
    }

    @Override
    public boolean existeEmail(String email) {
        String sql = "SELECT COUNT(1) FROM usuarios WHERE email = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.toLowerCase().strip());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error verificando email: " + email, e);
        }
    }

    // ── Mapper: ResultSet → Entidad de dominio ──

    private Usuario mapRow(ResultSet rs) throws SQLException {
        return Usuario.reconstituir(
                rs.getObject("id", UUID.class),
                rs.getObject("tienda_id", UUID.class),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("nombre"),
                Rol.valueOf(rs.getString("rol")),
                rs.getBoolean("activo"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}