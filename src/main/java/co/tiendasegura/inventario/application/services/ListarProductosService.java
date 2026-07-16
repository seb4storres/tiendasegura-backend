package co.tiendasegura.inventario.application.services;

import co.tiendasegura.inventario.application.dto.ListarProductosCommand;
import co.tiendasegura.inventario.application.dto.ProductoResponse;
import co.tiendasegura.inventario.application.mapper.ProductoMapper;
import co.tiendasegura.inventario.application.ports.in.ListarProductosUseCase;
import co.tiendasegura.inventario.domain.ports.out.ProductoRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Caso de uso: listar los productos de la tienda actual.
 */
public class ListarProductosService implements ListarProductosUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListarProductosService.class);

    private final ProductoRepositoryPort productoRepository;

    public ListarProductosService(ProductoRepositoryPort productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public List<ProductoResponse> ejecutar(ListarProductosCommand command) {
        log.debug("Listando productos de tienda {}", command.tiendaId());

        return productoRepository.listarPorTienda(command.tiendaId()).stream()
                .map(ProductoMapper::toResponse)
                .toList();
    }
}
