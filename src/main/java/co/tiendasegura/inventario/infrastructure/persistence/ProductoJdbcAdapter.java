package co.tiendasegura.inventario.infrastructure.persistence;

import co.tiendasegura.inventario.domain.model.Producto;
import co.tiendasegura.inventario.domain.ports.out.ProductoRepositoryPort;
import co.tiendasegura.shared.infrastructure.persistence.TenantConnectionHelper;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de salida: implementación JDBC del repositorio de Productos.
 *
 * A diferencia de los adapters de auth, este SÍ es tenant-scoped:
 *   - Aplica el tenant context a la conexión (SET LOCAL app.tenant_id) vía
 *     TenantConnectionHelper, dejando la conexión lista para cuando se
 *     habiliten políticas RLS en Postgres.
 *   - Además filtra explícitamente por tienda_id en cada query, como
 *     defensa adicional mientras no hay RLS activo.
 */
@Singleton
public class ProductoJdbcAdapter implements ProductoRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(ProductoJdbcAdapter.class);

    private final DataSource dataSource;
    private final TenantConnectionHelper tenantConnectionHelper;

    public ProductoJdbcAdapter(DataSource dataSource, TenantConnectionHelper tenantConnectionHelper) {
        this.dataSource = dataSource;
        this.tenantConnectionHelper = tenantConnectionHelper;
    }

    @Override
    public Producto guardar(Producto producto) {
        String sql = """
                INSERT INTO productos (id, tienda_id, codigo_barras, nombre, descripcion, categoria_id,
                                        precio_compra, precio_venta, stock, stock_minimo, activo, version,
                                        created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    codigo_barras = EXCLUDED.codigo_barras,
                    nombre = EXCLUDED.nombre,
                    descripcion = EXCLUDED.descripcion,
                    categoria_id = EXCLUDED.categoria_id,
                    precio_compra = EXCLUDED.precio_compra,
                    precio_venta = EXCLUDED.precio_venta,
                    stock = EXCLUDED.stock,
                    stock_minimo = EXCLUDED.stock_minimo,
                    activo = EXCLUDED.activo,
                    version = EXCLUDED.version,
                    updated_at = EXCLUDED.updated_at
                """;

        try (Connection conn = dataSource.getConnection()) {
            tenantConnectionHelper.applyTenantContext(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, producto.getId());
                ps.setObject(2, producto.getTiendaId());
                ps.setString(3, producto.getCodigoBarras());
                ps.setString(4, producto.getNombre());
                ps.setString(5, producto.getDescripcion());
                ps.setObject(6, producto.getCategoriaId());
                ps.setBigDecimal(7, producto.getPrecioCompra());
                ps.setBigDecimal(8, producto.getPrecioVenta());
                ps.setInt(9, producto.getStock());
                ps.setInt(10, producto.getStockMinimo());
                ps.setBoolean(11, producto.isActivo());
                ps.setInt(12, producto.getVersion());
                ps.setTimestamp(13, Timestamp.from(producto.getCreatedAt()));
                ps.setTimestamp(14, Timestamp.from(producto.getUpdatedAt()));

                ps.executeUpdate();
                log.debug("Producto guardado: {} ({})", producto.getNombre(), producto.getId());
                return producto;
            }

        } catch (SQLException e) {
            // Detectar violación del índice único (tienda_id, codigo_barras)
            if (e.getSQLState() != null && e.getSQLState().equals("23505")) {
                throw new RuntimeException("Código de barras duplicado: " + producto.getCodigoBarras(), e);
            }
            throw new RuntimeException("Error guardando producto: " + producto.getId(), e);
        }
    }

    @Override
    public Optional<Producto> buscarPorId(UUID tiendaId, UUID id) {
        String sql = "SELECT * FROM productos WHERE id = ? AND tienda_id = ?";

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
            throw new RuntimeException("Error buscando producto por ID: " + id, e);
        }
    }

    @Override
    public boolean existeCodigoBarras(UUID tiendaId, String codigoBarras) {
        String sql = "SELECT COUNT(1) FROM productos WHERE tienda_id = ? AND codigo_barras = ?";

        try (Connection conn = dataSource.getConnection()) {
            tenantConnectionHelper.applyTenantContext(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, tiendaId);
                ps.setString(2, codigoBarras);

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error verificando código de barras: " + codigoBarras, e);
        }
    }

    @Override
    public boolean descontarStock(UUID tiendaId, UUID productoId, int cantidad, int versionEsperada) {
        // UPDATE condicional atómico: stock y version se verifican y actualizan
        // en la misma sentencia. Si otra transacción ya modificó el producto
        // (version distinta) o no queda stock suficiente, 0 filas afectadas.
        String sql = """
                UPDATE productos
                SET stock = stock - ?, version = version + 1, updated_at = NOW()
                WHERE id = ? AND tienda_id = ? AND version = ? AND stock >= ?
                """;

        try (Connection conn = dataSource.getConnection()) {
            tenantConnectionHelper.applyTenantContext(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, cantidad);
                ps.setObject(2, productoId);
                ps.setObject(3, tiendaId);
                ps.setInt(4, versionEsperada);
                ps.setInt(5, cantidad);

                int filas = ps.executeUpdate();
                if (filas == 0) {
                    log.debug("Descuento de stock rechazado (version/stock no coincide): producto {} v{}",
                            productoId, versionEsperada);
                }
                return filas == 1;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error descontando stock del producto: " + productoId, e);
        }
    }

    // ── Mapper: ResultSet → Entidad de dominio ──

    private Producto mapRow(ResultSet rs) throws SQLException {
        return Producto.reconstituir(
                rs.getObject("id", UUID.class),
                rs.getObject("tienda_id", UUID.class),
                rs.getString("codigo_barras"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getObject("categoria_id", UUID.class),
                rs.getBigDecimal("precio_compra"),
                rs.getBigDecimal("precio_venta"),
                rs.getInt("stock"),
                rs.getInt("stock_minimo"),
                rs.getBoolean("activo"),
                rs.getInt("version"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
