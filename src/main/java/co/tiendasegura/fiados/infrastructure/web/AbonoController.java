package co.tiendasegura.fiados.infrastructure.web;

import co.tiendasegura.fiados.application.dto.AbonoResponse;
import co.tiendasegura.fiados.application.ports.in.RegistrarAbonoUseCase;
import co.tiendasegura.fiados.infrastructure.web.dto.RegistrarAbonoRequest;
import co.tiendasegura.shared.infrastructure.security.TenantContext;
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

@Controller("/abonos")
@Secured(SecurityRule.IS_AUTHENTICATED)
@ExecuteOn(TaskExecutors.BLOCKING)
@Validated
public class AbonoController {

    private final RegistrarAbonoUseCase registrarAbonoUseCase;

    public AbonoController(RegistrarAbonoUseCase registrarAbonoUseCase) {
        this.registrarAbonoUseCase = registrarAbonoUseCase;
    }

    @Post
    public HttpResponse<AbonoResponse> registrar(@Body @Valid RegistrarAbonoRequest request) {
        UUID tiendaId = TenantContext.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay tienda_id en el contexto. ¿Falta el JWT en la petición?"));

        AbonoResponse response = registrarAbonoUseCase.ejecutar(request.toCommand(tiendaId));
        return HttpResponse.status(HttpStatus.CREATED).body(response);
    }
}
