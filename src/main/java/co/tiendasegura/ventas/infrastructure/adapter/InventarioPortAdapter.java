package co.tiendasegura.ventas.infrastructure.adapter;

import co.tiendasegura.inventario.domain.model.Producto;
import co.tiendasegura.inventario.domain.ports.out.ProductoRepositoryPort;
import co.tiendasegura.ventas.domain.ports.out.InventarioPort;
import jakarta.inject.Singleton;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador que traduce entre el dominio de Ventas y el dominio de
 * Inventario. Es el ÚNICO punto del código donde `ventas` conoce tipos de
 * `inventario` — reutiliza el ProductoRepositoryPort ya existente, que es
 * quien posee la tabla `productos` y la lógica de concurrencia sobre ella
 * (ver ProductoJdbcAdapter#descontarStock).
 *
 * Al vivir en infraestructura, este acoplamiento no contamina el dominio
 * ni la aplicación de ventas: ambos siguen dependiendo únicamente de
 * InventarioPort, una interfaz propia de ventas.
 */
@Singleton
public class InventarioPortAdapter implements InventarioPort {

    private final ProductoRepositoryPort productoRepository;

    public InventarioPortAdapter(ProductoRepositoryPort productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public Optional<ProductoDisponible> buscarProducto(UUID tiendaId, UUID productoId) {
        return productoRepository.buscarPorId(tiendaId, productoId)
                .map(InventarioPortAdapter::toProductoDisponible);
    }

    @Override
    public boolean descontarStock(UUID tiendaId, UUID productoId, int cantidad, int versionEsperada) {
        return productoRepository.descontarStock(tiendaId, productoId, cantidad, versionEsperada);
    }

    private static ProductoDisponible toProductoDisponible(Producto producto) {
        return new ProductoDisponible(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecioVenta(),
                producto.getStock(),
                producto.getVersion(),
                producto.isActivo()
        );
    }
}
