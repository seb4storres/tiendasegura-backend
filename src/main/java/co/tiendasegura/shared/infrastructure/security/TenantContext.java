package co.tiendasegura.shared.infrastructure.security;

import io.micronaut.http.context.ServerRequestContext;

import java.util.Optional;
import java.util.UUID;

/**
 * Acceso al tenant (tienda_id / usuario_id) de la petición actual.
 *
 * Ciclo de vida:
 *   1. TenantSecurityFilter extrae tienda_id/usuario_id del JWT y los guarda
 *      como ATRIBUTOS del HttpRequest (request.setAttribute).
 *   2. Los repositorios de dominio leen getCurrentTenantId() en cualquier punto
 *      posterior de la petición, incluso en otro hilo.
 *
 * Por qué NO es un ThreadLocal: TenantSecurityFilter corre en el event loop
 * de Netty, pero los controllers anotados con @ExecuteOn(BLOCKING) — y por lo
 * tanto los repositorios JDBC que invocan — corren en un hilo distinto
 * (thread pool de bloqueo / virtual thread). Un ThreadLocal seteado en el
 * event loop NO es visible en ese otro hilo.
 *
 * ServerRequestContext.currentRequest() sí es propagado correctamente por
 * Micronaut entre esos hilos, porque viaja junto con el HttpRequest, no con
 * el hilo de ejecución. Guardar el tenant como atributo del request (en vez
 * de un ThreadLocal aparte) también evita el riesgo de fuga entre peticiones
 * si un hilo del pool se reutiliza sin limpiar el ThreadLocal.
 */
public final class TenantContext {

    private TenantContext() {}

    public static final String ATTR_TIENDA_ID = "tenant.tiendaId";
    public static final String ATTR_USUARIO_ID = "tenant.usuarioId";

    public record TenantInfo(UUID tiendaId, UUID usuarioId) {}

    public static Optional<UUID> getCurrentTenantId() {
        return ServerRequestContext.currentRequest()
                .flatMap(request -> request.getAttribute(ATTR_TIENDA_ID, UUID.class));
    }

    public static Optional<UUID> getCurrentUsuarioId() {
        return ServerRequestContext.currentRequest()
                .flatMap(request -> request.getAttribute(ATTR_USUARIO_ID, UUID.class));
    }

    public static Optional<TenantInfo> get() {
        Optional<UUID> tiendaId = getCurrentTenantId();
        Optional<UUID> usuarioId = getCurrentUsuarioId();
        if (tiendaId.isEmpty() || usuarioId.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new TenantInfo(tiendaId.get(), usuarioId.get()));
    }
}
