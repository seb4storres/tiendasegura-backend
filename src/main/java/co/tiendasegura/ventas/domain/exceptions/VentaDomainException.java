package co.tiendasegura.ventas.domain.exceptions;

/**
 * Excepción base para todos los errores del dominio de ventas.
 * Permite capturar cualquier error de dominio en un solo catch
 * en la capa de infraestructura (exception handler).
 */
public abstract class VentaDomainException extends RuntimeException {

    private final String codigoError;

    protected VentaDomainException(String mensaje, String codigoError) {
        super(mensaje);
        this.codigoError = codigoError;
    }

    public String getCodigoError() {
        return codigoError;
    }
}
