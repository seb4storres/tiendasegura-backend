package co.tiendasegura.inventario.application.ports.in;

import co.tiendasegura.inventario.application.dto.BuscarProductoCommand;
import co.tiendasegura.inventario.application.dto.ProductoResponse;

/**
 * Puerto de entrada: buscar un producto por ID dentro de la tienda actual.
 */
public interface BuscarProductoUseCase {

    ProductoResponse ejecutar(BuscarProductoCommand command);
}
