package co.tiendasegura.auth.infrastructure.security;

import co.tiendasegura.shared.infrastructure.security.TenantContext;
import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.security.authentication.Authentication;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

/**
 * Filtro HTTP que se ejecuta DESPUÉS del filtro de seguridad de Micronaut.
 *
 * Responsabilidades:
 *   1. Extrae tienda_id y user_id del JWT (ya validado por Micronaut Security).
 *   2. Los guarda como atributos del HttpRequest (no un ThreadLocal — ver
 *      TenantContext para el porqué).
 *
 * Para endpoints públicos (/auth/**), el JWT no existe → TenantContext queda vacío.
 * Los repositorios de auth están diseñados para operar sin tenant context.
 *
 * No hace falta limpiar nada al finalizar la petición: los atributos viven
 * en el HttpRequest, que se descarta junto con la petición.
 */
@Filter(Filter.MATCH_ALL_PATTERN)
public class TenantSecurityFilter implements HttpServerFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(TenantSecurityFilter.class);

    @Override
    public int getOrder() {
        // Ejecutar después del SecurityFilter de Micronaut (que está en SECURITY phase)
        return ServerFilterPhase.SECURITY.order() + 100;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        extractTenantFromAuthentication(request);
        return chain.proceed(request);
    }

    private void extractTenantFromAuthentication(HttpRequest<?> request) {
        request.getUserPrincipal().ifPresent(principal -> {
            if (principal instanceof Authentication auth) {
                Map<String, Object> attrs = auth.getAttributes();
                Object tiendaIdRaw = attrs.get("tienda_id");
                Object userIdRaw = attrs.get("user_id");

                if (tiendaIdRaw != null && userIdRaw != null) {
                    try {
                        UUID tiendaId = UUID.fromString(tiendaIdRaw.toString());
                        UUID userId = UUID.fromString(userIdRaw.toString());
                        request.setAttribute(TenantContext.ATTR_TIENDA_ID, tiendaId);
                        request.setAttribute(TenantContext.ATTR_USUARIO_ID, userId);
                        log.trace("Tenant context: tienda={}, usuario={}", tiendaId, userId);
                    } catch (IllegalArgumentException e) {
                        log.warn("Claims de tenant con formato UUID inválido: tienda={}, user={}",
                                tiendaIdRaw, userIdRaw);
                    }
                }
            }
        });
    }
}