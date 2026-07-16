package co.tiendasegura.inventario.application.ports.in;

import co.tiendasegura.inventario.application.dto.ListarProductosCommand;
import co.tiendasegura.inventario.application.dto.ProductoResponse;

import java.util.List;

/**
 * Puerto de entrada: listar los productos de la tienda actual.
 */
public interface ListarProductosUseCase {

    List<ProductoResponse> ejecutar(ListarProductosCommand command);
}
