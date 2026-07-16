package co.tiendasegura.ventas.domain.exceptions;

import java.util.UUID;

public class StockInsuficienteException extends VentaDomainException {

    public StockInsuficienteException(UUID productoId, int solicitado, int disponible) {
        super(
                "Stock insuficiente para el producto '%s': solicitado %d, disponible %d"
                        .formatted(productoId, solicitado, disponible),
                "VENTA_STOCK_INSUFICIENTE"
        );
    }
}
