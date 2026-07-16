package co.tiendasegura.fiados.domain.exceptions;

/**
 * Excepción base para todos los errores del dominio de fiados.
 * Permite capturar cualquier error de dominio en un solo catch
 * en la capa de infraestructura (exception handler).
 */
public abstract class FiadosDomainException extends RuntimeException {

    private final String codigoError;

    protected FiadosDomainException(String mensaje, String codigoError) {
        super(mensaje);
        this.codigoError = codigoError;
    }

    public String getCodigoError() {
        return codigoError;
    }
}
