package co.tiendasegura.inventario.infrastructure.config;

import co.tiendasegura.inventario.application.ports.in.BuscarProductoUseCase;
import co.tiendasegura.inventario.application.ports.in.CrearProductoUseCase;
import co.tiendasegura.inventario.application.ports.in.ListarProductosUseCase;
import co.tiendasegura.inventario.application.services.BuscarProductoService;
import co.tiendasegura.inventario.application.services.CrearProductoService;
import co.tiendasegura.inventario.application.services.ListarProductosService;
import co.tiendasegura.inventario.domain.ports.out.ProductoRepositoryPort;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

/**
 * Factory de Micronaut que ensambla los casos de uso del dominio de inventario.
 *
 * Aquí es donde el framework "toca" la capa de aplicación:
 *   - Los servicios se exponen como beans (@Singleton)
 *   - Se envuelven con @Transactional (la transacción abarca todo el caso de uso)
 *   - Las interfaces de los puertos de salida se resuelven por inyección
 *
 * Los servicios mismos NO tienen anotaciones de Micronaut.
 */
@Factory
public class InventarioUseCaseFactory {

    @Singleton
    @Transactional
    public CrearProductoUseCase crearProductoUseCase(ProductoRepositoryPort productoRepository) {
        return new CrearProductoService(productoRepository);
    }

    @Singleton
    @Transactional
    public BuscarProductoUseCase buscarProductoUseCase(ProductoRepositoryPort productoRepository) {
        return new BuscarProductoService(productoRepository);
    }

    @Singleton
    @Transactional
    public ListarProductosUseCase listarProductosUseCase(ProductoRepositoryPort productoRepository) {
        return new ListarProductosService(productoRepository);
    }
}
