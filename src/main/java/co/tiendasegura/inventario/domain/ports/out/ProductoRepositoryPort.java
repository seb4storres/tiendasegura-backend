package co.tiendasegura.inventario.domain.ports.out;

import co.tiendasegura.inventario.domain.model.Producto;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia de Productos.
 * Todas las operaciones son tenant-scoped: reciben tiendaId explícito
 * para evitar que un usuario acceda a productos de otra tienda.
 */
public interface ProductoRepositoryPort {

    Producto guardar(Producto producto);

    Optional<Producto> buscarPorId(UUID tiendaId, UUID id);

    boolean existeCodigoBarras(UUID tiendaId, String codigoBarras);

    /**
     * Descuenta stock de forma atómica, verificando la versión esperada
     * (optimistic concurrency control). Pensado para ser invocado desde
     * otros dominios (p. ej. Ventas) al confirmar una operación que
     * consume inventario.
     *
     * @return true si el descuento se aplicó; false si la versión no
     *         coincide o si el stock resultante sería negativo — es decir,
     *         alguien más modificó el producto entre la lectura y este
     *         descuento.
     */
    boolean descontarStock(UUID tiendaId, UUID productoId, int cantidad, int versionEsperada);
}
