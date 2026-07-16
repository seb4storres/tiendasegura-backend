package co.tiendasegura.inventario.application.mapper;

import co.tiendasegura.inventario.application.dto.ProductoResponse;
import co.tiendasegura.inventario.domain.model.Producto;

/**
 * Mapper manual (sin MapStruct ni ModelMapper) para transformaciones
 * entre entidades de dominio y DTOs de respuesta.
 * Clase utilitaria con métodos estáticos — no necesita estado.
 */
public final class ProductoMapper {

    private ProductoMapper() {
        // No instanciable
    }

    public static ProductoResponse toResponse(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getTiendaId(),
                producto.getCodigoBarras(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCategoriaId(),
                producto.getPrecioCompra(),
                producto.getPrecioVenta(),
                producto.getStock(),
                producto.getStockMinimo(),
                producto.isActivo(),
                producto.getVersion()
        );
    }
}
