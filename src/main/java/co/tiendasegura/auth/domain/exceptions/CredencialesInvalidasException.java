package co.tiendasegura.auth.domain.exceptions;

/**
 * Lanzada cuando email o password son incorrectos.
 * Mensaje genérico intencional: no revelar si el email existe o no.
 */
public class CredencialesInvalidasException extends AuthDomainException {

    public CredencialesInvalidasException() {
        super(
                "Credenciales inválidas",
                "AUTH_CREDENCIALES_INVALIDAS"
        );
    }
}