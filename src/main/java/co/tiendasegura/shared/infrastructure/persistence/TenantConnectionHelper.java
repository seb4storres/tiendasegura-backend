package co.tiendasegura.shared.infrastructure.persistence;

import co.tiendasegura.shared.infrastructure.security.TenantContext;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Helper compartido para inyectar el tenant_id en la conexión PostgreSQL.
 *
 * Usa set_config('app.tenant_id', value, true) donde:
 *   - true = LOCAL (scoped a la transacción actual)
 *   - Se resetea automáticamente al COMMIT/ROLLBACK
 *   - Si no hay transacción activa, aplica a nivel de sesión (safe: el pool
 *     lo sobreescribe en la siguiente petición, y el default es UUID-cero)
 *
 * IMPORTANTE: Los repositorios del dominio auth (login/registro) NO deben
 * llamar este helper. Operan cross-tenant para buscar email y crear tiendas.
 * Solo los repositorios de inventario, ventas y fiados lo necesitan.
 */
@Singleton
public class TenantConnectionHelper {

    private static final Logger log = LoggerFactory.getLogger(TenantConnectionHelper.class);

    private static final String SET_TENANT_SQL = "SELECT set_config('app.tenant_id', ?, true)";

    /**
     * Aplica el tenant_id del contexto actual a la conexión.
     * Debe llamarse al inicio de cada operación en repos multi-tenant.
     *
     * @throws IllegalStateException si no hay tenant en el contexto
     */
    public void applyTenantContext(Connection conn) throws SQLException {
        UUID tenantId = TenantContext.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay tenant_id en el contexto. ¿Falta el JWT o el endpoint es público?"));

        try (PreparedStatement ps = conn.prepareStatement(SET_TENANT_SQL)) {
            ps.setString(1, tenantId.toString());
            ps.execute();
            log.trace("Tenant context aplicado a conexión: {}", tenantId);
        }
    }

    /**
     * Versión silenciosa: aplica tenant si existe, no lanza excepción si no hay.
     * Útil para queries que pueden operar con o sin tenant.
     */
    public void applyTenantContextIfPresent(Connection conn) throws SQLException {
        var tenantId = TenantContext.getCurrentTenantId();
        if (tenantId.isPresent()) {
            try (PreparedStatement ps = conn.prepareStatement(SET_TENANT_SQL)) {
                ps.setString(1, tenantId.get().toString());
                ps.execute();
            }
        }
    }
}