package co.tiendasegura.auth.infrastructure.security;

import co.tiendasegura.auth.domain.model.Usuario;
import co.tiendasegura.auth.domain.ports.out.TokenProviderPort;
import io.micronaut.security.token.claims.ClaimsGenerator;
import io.micronaut.security.token.generator.AccessTokenConfiguration;
import io.micronaut.security.token.generator.TokenGenerator;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementación del puerto TokenProviderPort usando Micronaut Security JWT.
 * Genera access y refresh tokens firmados con HS256 (configurado en application.yml).
 *
 * Claims personalizados incluidos en el JWT:
 *   - sub: email del usuario (estándar JWT)
 *   - tienda_id: UUID de la tienda (para RLS en Postgres)
 *   - user_id: UUID del usuario
 *   - rol: ADMIN o CAJERO
 *
 * Nota: TokenGenerator solo expone generateToken(Map) y generateToken(Authentication, expiration) —
 * no hay overload de Map + expiration. Por eso el "exp" se calcula aquí vía ClaimsGenerator,
 * que además agrega iat/nbf de forma consistente con el resto de tokens de Micronaut Security.
 */
@Singleton
public class MicronautTokenProviderAdapter implements TokenProviderPort {

    private static final int REFRESH_TOKEN_EXPIRATION_SECONDS = 30 * 24 * 60 * 60; // 30 días

    private final TokenGenerator tokenGenerator;
    private final ClaimsGenerator claimsGenerator;
    private final AccessTokenConfiguration accessTokenConfiguration;

    public MicronautTokenProviderAdapter(TokenGenerator tokenGenerator,
                                         ClaimsGenerator claimsGenerator,
                                         AccessTokenConfiguration accessTokenConfiguration) {
        this.tokenGenerator = tokenGenerator;
        this.claimsGenerator = claimsGenerator;
        this.accessTokenConfiguration = accessTokenConfiguration;
    }

    @Override
    public String generarAccessToken(Usuario usuario) {
        // Expiration tomada de application.yml (access-token.expiration: 28800 = 8h)
        Map<String, Object> claims = claimsGenerator.generateClaimsSet(
                buildClaims(usuario), accessTokenConfiguration.getExpiration());
        return tokenGenerator.generateToken(claims)
                .orElseThrow(() -> new RuntimeException("Error generando access token"));
    }

    @Override
    public String generarRefreshToken(Usuario usuario) {
        Map<String, Object> baseClaims = buildClaims(usuario);
        baseClaims.put("token_type", "refresh");
        Map<String, Object> claims = claimsGenerator.generateClaimsSet(baseClaims, REFRESH_TOKEN_EXPIRATION_SECONDS);
        return tokenGenerator.generateToken(claims)
                .orElseThrow(() -> new RuntimeException("Error generando refresh token"));
    }

    private Map<String, Object> buildClaims(Usuario usuario) {
        // HashMap mutable: Micronaut puede agregar claims adicionales (iat, exp, nbf)
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", usuario.getEmail());
        claims.put("tienda_id", usuario.getTiendaId().toString());
        claims.put("user_id", usuario.getId().toString());
        claims.put("rol", usuario.getRol().name());
        return claims;
    }
}