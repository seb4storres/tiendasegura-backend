package co.tiendasegura.ventas.domain.exceptions;

import java.util.UUID;

public class ProductoNoDisponibleException extends VentaDomainException {

    public ProductoNoDisponibleException(UUID productoId) {
        super(
                "El producto '%s' no existe o no está disponible para la venta".formatted(productoId),
                "VENTA_PRODUCTO_NO_DISPONIBLE"
        );
    }
}
