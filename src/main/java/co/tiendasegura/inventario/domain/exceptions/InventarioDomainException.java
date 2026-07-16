package co.tiendasegura.inventario.domain.exceptions;

/**
 * Excepción base para todos los errores del dominio de inventario.
 * Permite capturar cualquier error de dominio en un solo catch
 * en la capa de infraestructura (exception handler).
 */
public abstract class InventarioDomainException extends RuntimeException {

    private final String codigoError;

    protected InventarioDomainException(String mensaje, String codigoError) {
        super(mensaje);
        this.codigoError = codigoError;
    }

    public String getCodigoError() {
        return codigoError;
    }
}
