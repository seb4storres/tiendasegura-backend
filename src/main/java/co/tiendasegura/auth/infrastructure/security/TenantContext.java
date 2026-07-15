package co.tiendasegura.auth.infrastructure.security;

import java.util.Optional;
import java.util.UUID;

/**
 * Almacén ThreadLocal del contexto del tenant para la petición actual.
 *
 * Ciclo de vida:
 *   1. TenantSecurityFilter extrae tienda_id del JWT → llama set()
 *   2. Los repositorios de dominio leen getCurrentTenantId()
 *   3. TenantSecurityFilter llama clear() al finalizar la petición
 *
 * Para la propagación entre event-loop → blocking thread,
 * Micronaut 4 propaga el ServerRequestContext automáticamente con
 * @ExecuteOn(BLOCKING). Los repositorios de auth (login/registro)
 * NO necesitan tenant context — operan cross-tenant.
 */
public final class TenantContext {

    private TenantContext() {}

    public record TenantInfo(UUID tiendaId, UUID usuarioId) {}

    private static final ThreadLocal<TenantInfo> CONTEXT = new ThreadLocal<>();

    public static void set(UUID tiendaId, UUID usuarioId) {
        CONTEXT.set(new TenantInfo(tiendaId, usuarioId));
    }

    public static Optional<UUID> getCurrentTenantId() {
        return Optional.ofNullable(CONTEXT.get()).map(TenantInfo::tiendaId);
    }

    public static Optional<UUID> getCurrentUsuarioId() {
        return Optional.ofNullable(CONTEXT.get()).map(TenantInfo::usuarioId);
    }

    public static Optional<TenantInfo> get() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static void clear() {
        CONTEXT.remove();
    }
}