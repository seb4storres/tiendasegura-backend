package co.tiendasegura.fiados.infrastructure.persistence;

import co.tiendasegura.fiados.domain.model.Abono;
import co.tiendasegura.fiados.domain.ports.out.AbonoRepositoryPort;
import co.tiendasegura.shared.infrastructure.persistence.TenantConnectionHelper;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

@Singleton
public class AbonoJdbcAdapter implements AbonoRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(AbonoJdbcAdapter.class);

    private final DataSource dataSource;
    private final TenantConnectionHelper tenantConnectionHelper;

    public AbonoJdbcAdapter(DataSource dataSource, TenantConnectionHelper tenantConnectionHelper) {
        this.dataSource = dataSource;
        this.tenantConnectionHelper = tenantConnectionHelper;
    }

    @Override
    public Abono guardar(Abono abono) {
        String sql = """
                INSERT INTO abonos (id, tienda_id, cliente_id, monto, metodo_pago, nota, fecha, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection()) {
            tenantConnectionHelper.applyTenantContext(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, abono.getId());
                ps.setObject(2, abono.getTiendaId());
                ps.setObject(3, abono.getClienteId());
                ps.setBigDecimal(4, abono.getMonto());
                ps.setString(5, abono.getMetodoPago());
                ps.setString(6, abono.getNota());
                ps.setTimestamp(7, Timestamp.from(abono.getFecha()));
                ps.setTimestamp(8, Timestamp.from(abono.getCreatedAt()));
                ps.executeUpdate();

                log.debug("Abono guardado: {} ({})", abono.getMonto(), abono.getId());
                return abono;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error guardando abono: " + abono.getId(), e);
        }
    }
}
