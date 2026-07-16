package co.tiendasegura.ventas.domain.ports.out;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida hacia el dominio de Inventario.
 *
 * Ventas NO conoce las clases de dominio de Inventario (Producto, etc.):
 * define aquí su propio contrato mínimo (ProductoDisponible), y es la capa
 * de infraestructura (InventarioPortAdapter) la única que traduce entre
 * ambos dominios. Esto mantiene a cada dominio autónomo — inventario podría
 * cambiar su modelo interno sin romper el dominio de ventas mientras este
 * puerto se siga cumpliendo.
 */
public interface InventarioPort {

    Optional<ProductoDisponible> buscarProducto(UUID tiendaId, UUID productoId);

    /**
     * Descuenta stock de forma atómica respetando optimistic concurrency
     * control (version).
     *
     * @return true si se aplicó el descuento; false si la versión no
     *         coincide o no hay stock suficiente — alguien más modificó
     *         el producto entre la lectura y este descuento.
     */
    boolean descontarStock(UUID tiendaId, UUID productoId, int cantidad, int versionEsperada);

    record ProductoDisponible(UUID id, String nombre, BigDecimal precioVenta,
                              int stock, int version, boolean activo) {}
}
