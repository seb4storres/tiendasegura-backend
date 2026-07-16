package co.tiendasegura.ventas.infrastructure.persistence;

import co.tiendasegura.shared.infrastructure.persistence.TenantConnectionHelper;
import co.tiendasegura.ventas.domain.exceptions.VentaDuplicadaException;
import co.tiendasegura.ventas.domain.model.DetalleVenta;
import co.tiendasegura.ventas.domain.model.EstadoVenta;
import co.tiendasegura.ventas.domain.model.MetodoPago;
import co.tiendasegura.ventas.domain.model.Venta;
import co.tiendasegura.ventas.domain.ports.out.VentaRepositoryPort;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de salida: implementación JDBC del repositorio de Ventas.
 *
 * guardar() inserta la venta y todos sus detalles en la misma conexión.
 * Como corre dentro del @Transactional del caso de uso (ver
 * VentasUseCaseFactory), ese insert y el descuento de stock hecho antes
 * por InventarioPort son atómicos: todo o nada.
 *
 * A propósito NO usa ON CONFLICT en el insert de `ventas`: un choque de PK
 * (mismo id de venta) es exactamente la señal de una solicitud duplicada
 * concurrente, y se traduce a VentaDuplicadaException para que el caso de
 * uso la distinga de un error real.
 */
@Singleton
public class VentaJdbcAdapter implements VentaRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(VentaJdbcAdapter.class);

    private final DataSource dataSource;
    private final TenantConnectionHelper tenantConnectionHelper;

    public VentaJdbcAdapter(DataSource dataSource, TenantConnectionHelper tenantConnectionHelper) {
        this.dataSource = dataSource;
        this.tenantConnectionHelper = tenantConnectionHelper;
    }

    @Override
    public Venta guardar(Venta venta) {
        String sqlVenta = """
                INSERT INTO ventas (id, tienda_id, usuario_id, cliente_id, fecha, subtotal, descuento,
                                     total, metodo_pago, monto_recibido, cambio, estado, notas, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        String sqlDetalle = """
                INSERT INTO detalle_ventas (id, venta_id, tienda_id, producto_id, cantidad,
                                             precio_unitario_snapshot, nombre_producto_snapshot, subtotal, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection()) {
            tenantConnectionHelper.applyTenantContext(conn);

            try (PreparedStatement ps = conn.prepareStatement(sqlVenta)) {
                ps.setObject(1, venta.getId());
                ps.setObject(2, venta.getTiendaId());
                ps.setObject(3, venta.getCajeroId());
                ps.setObject(4, venta.getClienteId());
                ps.setTimestamp(5, Timestamp.from(venta.getFecha()));
                ps.setBigDecimal(6, venta.getSubtotal());
                ps.setBigDecimal(7, venta.getDescuento());
                ps.setBigDecimal(8, venta.getTotal());
                ps.setString(9, venta.getMetodoPago().name());
                ps.setBigDecimal(10, venta.getMontoRecibido());
                ps.setBigDecimal(11, venta.getCambio());
                ps.setString(12, venta.getEstado().name());
                ps.setString(13, venta.getNotas());
                ps.setTimestamp(14, Timestamp.from(venta.getCreatedAt()));
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlDetalle)) {
                for (DetalleVenta detalle : venta.getDetalles()) {
                    ps.setObject(1, detalle.getId());
                    ps.setObject(2, detalle.getVentaId());
                    ps.setObject(3, venta.getTiendaId());
                    ps.setObject(4, detalle.getProductoId());
                    ps.setInt(5, detalle.getCantidad());
                    ps.setBigDecimal(6, detalle.getPrecioUnitarioSnapshot());
                    ps.setString(7, detalle.getNombreProductoSnapshot());
                    ps.setBigDecimal(8, detalle.getSubtotal());
                    ps.setTimestamp(9, Timestamp.from(detalle.getCreatedAt()));
                    ps.executeUpdate();
                }
            }

            log.debug("Venta guardada: {} ({} detalles)", venta.getId(), venta.getDetalles().size());
            return venta;

        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().equals("23505")) {
                throw new VentaDuplicadaException(venta.getId());
            }
            throw new RuntimeException("Error guardando venta: " + venta.getId(), e);
        }
    }

    @Override
    public Optional<Venta> buscarPorId(UUID tiendaId, UUID id) {
        String sqlVenta = "SELECT * FROM ventas WHERE id = ? AND tienda_id = ?";
        String sqlDetalles = "SELECT * FROM detalle_ventas WHERE venta_id = ? AND tienda_id = ? ORDER BY created_at";

        try (Connection conn = dataSource.getConnection()) {
            tenantConnectionHelper.applyTenantContext(conn);

            VentaRow ventaRow;
            try (PreparedStatement ps = conn.prepareStatement(sqlVenta)) {
                ps.setObject(1, id);
                ps.setObject(2, tiendaId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    ventaRow = mapVentaRow(rs);
                }
            }

            List<DetalleVenta> detalles = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sqlDetalles)) {
                ps.setObject(1, id);
                ps.setObject(2, tiendaId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        detalles.add(mapDetalleRow(rs));
                    }
                }
            }

            return Optional.of(ventaRow.toVenta(detalles));

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando venta por ID: " + id, e);
        }
    }

    // ── Mappers: ResultSet → Entidad de dominio ──

    private VentaRow mapVentaRow(ResultSet rs) throws SQLException {
        return new VentaRow(
                rs.getObject("id", UUID.class),
                rs.getObject("tienda_id", UUID.class),
                rs.getObject("usuario_id", UUID.class),
                rs.getObject("cliente_id", UUID.class),
                rs.getTimestamp("fecha").toInstant(),
                rs.getBigDecimal("subtotal"),
                rs.getBigDecimal("descuento"),
                rs.getBigDecimal("total"),
                MetodoPago.valueOf(rs.getString("metodo_pago")),
                rs.getBigDecimal("monto_recibido"),
                rs.getBigDecimal("cambio"),
                EstadoVenta.valueOf(rs.getString("estado")),
                rs.getString("notas"),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    private DetalleVenta mapDetalleRow(ResultSet rs) throws SQLException {
        return DetalleVenta.reconstituir(
                rs.getObject("id", UUID.class),
                rs.getObject("venta_id", UUID.class),
                rs.getObject("producto_id", UUID.class),
                rs.getString("nombre_producto_snapshot"),
                rs.getInt("cantidad"),
                rs.getBigDecimal("precio_unitario_snapshot"),
                rs.getBigDecimal("subtotal"),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    /** Fila cruda de `ventas`, previa a ensamblar el agregado completo con sus detalles. */
    private record VentaRow(UUID id, UUID tiendaId, UUID cajeroId, UUID clienteId, Instant fecha,
                            BigDecimal subtotal, BigDecimal descuento, BigDecimal total,
                            MetodoPago metodoPago, BigDecimal montoRecibido, BigDecimal cambio,
                            EstadoVenta estado, String notas, Instant createdAt) {
        Venta toVenta(List<DetalleVenta> detalles) {
            return Venta.reconstituir(id, tiendaId, cajeroId, clienteId, fecha, subtotal, descuento, total,
                    metodoPago, montoRecibido, cambio, estado, notas, createdAt, detalles);
        }
    }
}
