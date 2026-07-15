package co.tiendasegura.auth.domain.exceptions;

public class EmailYaRegistradoException extends AuthDomainException {

    public EmailYaRegistradoException(String email) {
        super(
                "El correo '%s' ya está registrado en el sistema".formatted(email),
                "AUTH_EMAIL_DUPLICADO"
        );
    }
}