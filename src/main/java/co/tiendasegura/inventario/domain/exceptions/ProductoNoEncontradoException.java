package co.tiendasegura.inventario.domain.exceptions;

import java.util.UUID;

public class ProductoNoEncontradoException extends InventarioDomainException {

    public ProductoNoEncontradoException(UUID id) {
        super(
                "No se encontró el producto con ID '%s'".formatted(id),
                "INVENTARIO_PRODUCTO_NO_ENCONTRADO"
        );
    }
}
