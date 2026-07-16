package co.tiendasegura.inventario.domain.exceptions;

public class CodigoBarrasDuplicadoException extends InventarioDomainException {

    public CodigoBarrasDuplicadoException(String codigoBarras) {
        super(
                "El código de barras '%s' ya está registrado en esta tienda".formatted(codigoBarras),
                "INVENTARIO_CODIGO_BARRAS_DUPLICADO"
        );
    }
}
