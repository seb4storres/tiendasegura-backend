package co.tiendasegura.fiados.infrastructure.web;

import co.tiendasegura.fiados.application.dto.BuscarClienteCommand;
import co.tiendasegura.fiados.application.dto.ClienteResponse;
import co.tiendasegura.fiados.application.ports.in.BuscarClienteUseCase;
import co.tiendasegura.fiados.application.ports.in.CrearClienteUseCase;
import co.tiendasegura.fiados.infrastructure.web.dto.CrearClienteRequest;
import co.tiendasegura.shared.infrastructure.security.TenantContext;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;

import java.util.UUID;

@Controller("/clientes")
@Secured(SecurityRule.IS_AUTHENTICATED)
@ExecuteOn(TaskExecutors.BLOCKING)
@Validated
public class ClienteController {

    private final CrearClienteUseCase crearClienteUseCase;
    private final BuscarClienteUseCase buscarClienteUseCase;

    public ClienteController(CrearClienteUseCase crearClienteUseCase, BuscarClienteUseCase buscarClienteUseCase) {
        this.crearClienteUseCase = crearClienteUseCase;
        this.buscarClienteUseCase = buscarClienteUseCase;
    }

    @Post
    public HttpResponse<ClienteResponse> crear(@Body @Valid CrearClienteRequest request) {
        ClienteResponse response = crearClienteUseCase.ejecutar(request.toCommand(tiendaIdActual()));
        return HttpResponse.status(HttpStatus.CREATED).body(response);
    }

    @Get("/{id}")
    public HttpResponse<ClienteResponse> buscar(@PathVariable UUID id) {
        ClienteResponse response = buscarClienteUseCase.ejecutar(new BuscarClienteCommand(tiendaIdActual(), id));
        return HttpResponse.ok(response);
    }

    private UUID tiendaIdActual() {
        return TenantContext.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay tienda_id en el contexto. ¿Falta el JWT en la petición?"));
    }
}
