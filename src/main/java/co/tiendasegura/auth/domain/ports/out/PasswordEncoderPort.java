package co.tiendasegura.auth.domain.ports.out;

/**
 * Puerto de salida para encriptación de contraseñas.
 * Invierte la dependencia: el dominio define QUÉ necesita,
 * la infraestructura decide CÓMO (BCrypt, Argon2, etc.).
 */
public interface PasswordEncoderPort {

    /**
     * Genera el hash de un password en texto plano.
     */
    String codificar(String passwordPlano);

    /**
     * Verifica si un password en texto plano coincide con su hash.
     */
    boolean verificar(String passwordPlano, String passwordHash);
}