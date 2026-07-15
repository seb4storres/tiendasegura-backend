package co.tiendasegura.auth.domain.exceptions;

/**
 * Excepción base para todos los errores del dominio de autenticación.
 * Permite capturar cualquier error de dominio en un solo catch
 * en la capa de infraestructura (controller advice / exception handler).
 */
public abstract class AuthDomainException extends RuntimeException {

    private final String codigoError;

    protected AuthDomainException(String mensaje, String codigoError) {
        super(mensaje);
        this.codigoError = codigoError;
    }

    public String getCodigoError() {
        return codigoError;
    }
}