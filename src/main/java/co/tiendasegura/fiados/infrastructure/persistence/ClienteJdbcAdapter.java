package co.tiendasegura.fiados.infrastructure.persistence;

import co.tiendasegura.fiados.domain.exceptions.ClienteModificadoConcurrentementeException;
import co.tiendasegura.fiados.domain.model.Cliente;
import co.tiendasegura.fiados.domain.model.EstadoCliente;
import co.tiendasegura.fiados.domain.ports.out.ClienteRepositoryPort;
import co.tiendasegura.shared.infrastructure.persistence.TenantConnectionHelper;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de salida: implementación JDBC del repositorio de Clientes.
 *
 * guardar() usa INSERT ... ON CONFLICT (id) DO UPDATE ... WHERE version = ?
 * — el mismo truco que Producto usa en descontarStock(), pero aplicado al
 * upsert genérico: si la fila ya existe (UPDATE) pero la version no
 * coincide con la que se leyó, la cláusula WHERE del DO UPDATE hace que
 * Postgres no toque la fila y el statement afecta 0 filas — la señal de
 * que otra operación concurrente modificó el cliente primero. Si la fila
 * no existe (INSERT puro), la cláusula WHERE ni siquiera aplica.
 */
@Singleton
public class ClienteJdbcAdapter implements ClienteRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(ClienteJdbcAdapter.class);

    private final DataSource dataSource;
    private final TenantConnectionHelper tenantConnectionHelper;

    public ClienteJdbcAdapter(DataSource dataSource, TenantConnectionHelper tenantConnectionHelper) {
        this.dataSource = dataSource;
        this.tenantConnectionHelper = tenantConnectionHelper;
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        String sql = """
                INSERT INTO clientes (id, tienda_id, nombre, telefono, cedula, direccion,
                                       limite_credito, saldo_pendiente, estado, version, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    nombre = EXCLUDED.nombre,
                    telefono = EXCLUDED.telefono,
                    cedula = EXCLUDED.cedula,
                    direccion = EXCLUDED.direccion,
                    limite_credito = EXCLUDED.limite_credito,
                    saldo_pendiente = EXCLUDED.saldo_pendiente,
                    estado = EXCLUDED.estado,
                    version = EXCLUDED.version,
                    updated_at = EXCLUDED.updated_at
                WHERE clientes.version = ?
                """;

        try (Connection conn = dataSource.getConnection()) {
            tenantConnectionHelper.applyTenantContext(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, cliente.getId());
                ps.setObject(2, cliente.getTiendaId());
                ps.setString(3, cliente.getNombre());
                ps.setString(4, cliente.getTelefono());
                ps.setString(5, cliente.getCedula());
                ps.setString(6, cliente.getDireccion());
                ps.setBigDecimal(7, cliente.getLimiteCredito());
                ps.setBigDecimal(8, cliente.getSaldoActual());
                ps.setString(9, cliente.getEstado().name());
                ps.setInt(10, cliente.getVersion());
                ps.setTimestamp(11, Timestamp.from(cliente.getCreatedAt()));
                ps.setTimestamp(12, Timestamp.from(cliente.getUpdatedAt()));
                // version "anterior" — el dominio ya incrementó cliente.getVersion()
                // en memoria antes de llamar guardar(); esto es lo que debía haber
                // en la fila justo antes de esta escritura. Irrelevante en el caso
                // de un INSERT puro (no hay conflicto que evaluar).
                ps.setInt(13, cliente.getVersion() - 1);

                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new ClienteModificadoConcurrentementeException(cliente.getId());
                }
                log.debug("Cliente guardado: {} ({})", cliente.getNombre(), cliente.getId());
                return cliente;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error guardando cliente: " + cliente.getId(), e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(UUID tiendaId, UUID id) {
        String sql = "SELECT * FROM clientes WHERE id = ? AND tienda_id = ?";

        try (Connection conn = dataSource.getConnection()) {
            tenantConnectionHelper.applyTenantContext(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, id);
                ps.setObject(2, tiendaId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando cliente por ID: " + id, e);
        }
    }

    // ── Mapper: ResultSet → Entidad de dominio ──

    private Cliente mapRow(ResultSet rs) throws SQLException {
        return Cliente.reconstituir(
                rs.getObject("id", UUID.class),
                rs.getObject("tienda_id", UUID.class),
                rs.getString("nombre"),
                rs.getString("telefono"),
                rs.getString("cedula"),
                rs.getString("direccion"),
                rs.getBigDecimal("limite_credito"),
                rs.getBigDecimal("saldo_pendiente"),
                EstadoCliente.valueOf(rs.getString("estado")),
                rs.getInt("version"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
