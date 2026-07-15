package co.tiendasegura.auth.domain.exceptions;

public class UsuarioInactivoException extends AuthDomainException {

    public UsuarioInactivoException(String email) {
        super(
                "La cuenta asociada a '%s' se encuentra desactivada".formatted(email),
                "AUTH_USUARIO_INACTIVO"
        );
    }
}