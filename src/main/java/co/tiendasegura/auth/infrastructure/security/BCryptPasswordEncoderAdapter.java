package co.tiendasegura.auth.infrastructure.security;

import co.tiendasegura.auth.domain.ports.out.PasswordEncoderPort;
import jakarta.inject.Singleton;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Implementación del puerto PasswordEncoderPort usando BCrypt.
 * Cost factor 12: buen balance entre seguridad y velocidad en hardware típico.
 *
 * Requiere dependencia en build.gradle:
 *   implementation("org.mindrot:jbcrypt:0.4")
 */
@Singleton
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private static final int COST_FACTOR = 12;

    @Override
    public String codificar(String passwordPlano) {
        return BCrypt.hashpw(passwordPlano, BCrypt.gensalt(COST_FACTOR));
    }

    @Override
    public boolean verificar(String passwordPlano, String passwordHash) {
        try {
            return BCrypt.checkpw(passwordPlano, passwordHash);
        } catch (IllegalArgumentException e) {
            // Hash con formato inválido → no coincide
            return false;
        }
    }
}