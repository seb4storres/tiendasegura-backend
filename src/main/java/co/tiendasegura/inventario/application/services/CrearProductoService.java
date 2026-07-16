package co.tiendasegura.inventario.application.services;

import co.tiendasegura.inventario.application.dto.CrearProductoCommand;
import co.tiendasegura.inventario.application.dto.ProductoResponse;
import co.tiendasegura.inventario.application.mapper.ProductoMapper;
import co.tiendasegura.inventario.application.ports.in.CrearProductoUseCase;
import co.tiendasegura.inventario.domain.exceptions.CodigoBarrasDuplicadoException;
import co.tiendasegura.inventario.domain.model.Producto;
import co.tiendasegura.inventario.domain.ports.out.ProductoRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caso de uso: crear un producto nuevo en el inventario de la tienda.
 *
 * Flujo:
 * 1. Verificar que el código de barras no esté duplicado en la tienda (si se envió).
 * 2. Crear la entidad Producto (con UUID generado internamente).
 * 3. Persistir el producto.
 * 4. Mapear y retornar.
 *
 * La anotación @Singleton y @Transactional se aplican en la configuración
 * de infraestructura, no aquí. Este servicio es Java puro + SLF4J.
 */
public class CrearProductoService implements CrearProductoUseCase {

    private static final Logger log = LoggerFactory.getLogger(CrearProductoService.class);

    private final ProductoRepositoryPort productoRepository;

    public CrearProductoService(ProductoRepositoryPort productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public ProductoResponse ejecutar(CrearProductoCommand command) {
        log.info("Creando producto '{}' para tienda {}", command.nombre(), command.tiendaId());

        if (command.codigoBarras() != null
                && productoRepository.existeCodigoBarras(command.tiendaId(), command.codigoBarras())) {
            throw new CodigoBarrasDuplicadoException(command.codigoBarras());
        }

        Producto producto = Producto.crear(
                command.tiendaId(),
                command.nombre(),
                command.codigoBarras(),
                command.descripcion(),
                command.categoriaId(),
                command.precioCompra(),
                command.precioVenta(),
                command.stockInicial(),
                command.stockMinimo()
        );
        producto = productoRepository.guardar(producto);

        log.debug("Producto creado con ID: {}", producto.getId());

        return ProductoMapper.toResponse(producto);
    }
}
