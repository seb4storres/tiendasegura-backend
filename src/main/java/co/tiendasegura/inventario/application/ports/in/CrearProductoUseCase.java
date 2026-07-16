package co.tiendasegura.inventario.application.ports.in;

import co.tiendasegura.inventario.application.dto.CrearProductoCommand;
import co.tiendasegura.inventario.application.dto.ProductoResponse;

/**
 * Puerto de entrada: crear un producto nuevo en el inventario de la tienda.
 */
public interface CrearProductoUseCase {

    ProductoResponse ejecutar(CrearProductoCommand command);
}
