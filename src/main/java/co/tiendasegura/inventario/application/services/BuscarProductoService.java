package co.tiendasegura.inventario.application.services;

import co.tiendasegura.inventario.application.dto.BuscarProductoCommand;
import co.tiendasegura.inventario.application.dto.ProductoResponse;
import co.tiendasegura.inventario.application.mapper.ProductoMapper;
import co.tiendasegura.inventario.application.ports.in.BuscarProductoUseCase;
import co.tiendasegura.inventario.domain.exceptions.ProductoNoEncontradoException;
import co.tiendasegura.inventario.domain.model.Producto;
import co.tiendasegura.inventario.domain.ports.out.ProductoRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caso de uso: buscar un producto por ID dentro de la tienda actual.
 */
public class BuscarProductoService implements BuscarProductoUseCase {

    private static final Logger log = LoggerFactory.getLogger(BuscarProductoService.class);

    private final ProductoRepositoryPort productoRepository;

    public BuscarProductoService(ProductoRepositoryPort productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public ProductoResponse ejecutar(BuscarProductoCommand command) {
        log.debug("Buscando producto {} en tienda {}", command.id(), command.tiendaId());

        Producto producto = productoRepository.buscarPorId(command.tiendaId(), command.id())
                .orElseThrow(() -> new ProductoNoEncontradoException(command.id()));

        return ProductoMapper.toResponse(producto);
    }
}
