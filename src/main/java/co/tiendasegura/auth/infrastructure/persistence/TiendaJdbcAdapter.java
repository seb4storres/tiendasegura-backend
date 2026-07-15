package co.tiendasegura.auth.infrastructure.persistence;

import co.tiendasegura.auth.domain.model.Plan;
import co.tiendasegura.auth.domain.model.Tienda;
import co.tiendasegura.auth.domain.ports.out.TiendaRepositoryPort;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de salida: implementación JDBC del repositorio de Tiendas.
 *
 * NO aplica tenant context (SET LOCAL) porque:
 *   - La tabla tiendas NO tiene RLS (es la tabla raíz del tenant).
 *   - Se accede durante registro (no hay JWT aún) y login (para verificar activa).
 */
@Singleton
public class TiendaJdbcAdapter implements TiendaRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(TiendaJdbcAdapter.class);

    private final DataSource dataSource;

    public TiendaJdbcAdapter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Tienda guardar(Tienda tienda) {
        String sql = """
                INSERT INTO tiendas (id, nombre, nit, direccion, telefono, plan, activa, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    nombre = EXCLUDED.nombre,
                    nit = EXCLUDED.nit,
                    direccion = EXCLUDED.direccion,
                    telefono = EXCLUDED.telefono,
                    plan = EXCLUDED.plan,
                    activa = EXCLUDED.activa,
                    updated_at = EXCLUDED.updated_at
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, tienda.getId());
            ps.setString(2, tienda.getNombre());
            ps.setString(3, tienda.getNit());
            ps.setString(4, tienda.getDireccion());
            ps.setString(5, tienda.getTelefono());
            ps.setString(6, tienda.getPlan().name());
            ps.setBoolean(7, tienda.isActiva());
            ps.setTimestamp(8, Timestamp.from(tienda.getCreatedAt()));
            ps.setTimestamp(9, Timestamp.from(tienda.getUpdatedAt()));

            ps.executeUpdate();
            log.debug("Tienda guardada: {} ({})", tienda.getNombre(), tienda.getId());
            return tienda;

        } catch (SQLException e) {
            throw new RuntimeException("Error guardando tienda: " + tienda.getId(), e);
        }
    }

    @Override
    public Optional<Tienda> buscarPorId(UUID id) {
        String sql = "SELECT * FROM tiendas WHERE id = ?";

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
            throw new RuntimeException("Error buscando tienda por ID: " + id, e);
        }
    }

    @Override
    public boolean existePorNit(String nit) {
        if (nit == null || nit.isBlank()) {
            return false;
        }

        String sql = "SELECT COUNT(1) FROM tiendas WHERE nit = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nit);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error verificando NIT: " + nit, e);
        }
    }

    // ── Mapper: ResultSet → Entidad de dominio ──

    private Tienda mapRow(ResultSet rs) throws SQLException {
        return Tienda.reconstituir(
                rs.getObject("id", UUID.class),
                rs.getString("nombre"),
                rs.getString("nit"),
                rs.getString("direccion"),
                rs.getString("telefono"),
                Plan.valueOf(rs.getString("plan")),
                rs.getBoolean("activa"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}