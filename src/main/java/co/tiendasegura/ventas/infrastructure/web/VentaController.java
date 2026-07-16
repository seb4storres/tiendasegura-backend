package co.tiendasegura.ventas.infrastructure.web;

import co.tiendasegura.shared.infrastructure.security.TenantContext;
import co.tiendasegura.ventas.application.dto.VentaResponse;
import co.tiendasegura.ventas.application.ports.in.RegistrarVentaUseCase;
import co.tiendasegura.ventas.infrastructure.web.dto.CrearVentaRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;

import java.util.UUID;

/**
 * Adaptador de entrada HTTP para ventas.
 *
 * cajeroId sale del tenant context (claim user_id del JWT) — nunca del
 * body del cliente: quien registra la venta es siempre el usuario
 * autenticado, no alguien que el cliente pueda declarar.
 */
@Controller("/ventas")
@Secured(SecurityRule.IS_AUTHENTICATED)
@ExecuteOn(TaskExecutors.BLOCKING)
@Validated
public class VentaController {

    private final RegistrarVentaUseCase registrarVentaUseCase;

    public VentaController(RegistrarVentaUseCase registrarVentaUseCase) {
        this.registrarVentaUseCase = registrarVentaUseCase;
    }

    @Post
    public HttpResponse<VentaResponse> registrar(@Body @Valid CrearVentaRequest request) {
        UUID tiendaId = TenantContext.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay tienda_id en el contexto. ¿Falta el JWT en la petición?"));
        UUID cajeroId = TenantContext.getCurrentUsuarioId()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay usuario_id en el contexto. ¿Falta el JWT en la petición?"));

        VentaResponse response = registrarVentaUseCase.ejecutar(request.toCommand(tiendaId, cajeroId));
        return HttpResponse.status(HttpStatus.CREATED).body(response);
    }
}
