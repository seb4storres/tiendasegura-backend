package co.tiendasegura.inventario.infrastructure.web;

import co.tiendasegura.inventario.application.dto.BuscarProductoCommand;
import co.tiendasegura.inventario.application.dto.ListarProductosCommand;
import co.tiendasegura.inventario.application.dto.ProductoResponse;
import co.tiendasegura.inventario.application.ports.in.BuscarProductoUseCase;
import co.tiendasegura.inventario.application.ports.in.CrearProductoUseCase;
import co.tiendasegura.inventario.application.ports.in.ListarProductosUseCase;
import co.tiendasegura.inventario.infrastructure.web.dto.CrearProductoRequest;
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

import java.util.List;
import java.util.UUID;

/**
 * Adaptador de entrada HTTP para el inventario.
 *
 * - @Secured(IS_AUTHENTICATED): requiere JWT válido (a diferencia de /auth/**).
 * - @ExecuteOn(BLOCKING): las operaciones JDBC bloquean; se ejecutan
 *   en el thread pool de I/O, no en el event loop de Netty.
 * - El tiendaId NUNCA se lee del body/query del cliente: siempre sale
 *   del TenantContext, poblado por TenantSecurityFilter desde el JWT.
 */
@Controller("/productos")
@Secured(SecurityRule.IS_AUTHENTICATED)
@ExecuteOn(TaskExecutors.BLOCKING)
@Validated
public class ProductoController {

    private final CrearProductoUseCase crearProductoUseCase;
    private final BuscarProductoUseCase buscarProductoUseCase;
    private final ListarProductosUseCase listarProductosUseCase;

    public ProductoController(CrearProductoUseCase crearProductoUseCase,
                              BuscarProductoUseCase buscarProductoUseCase,
                              ListarProductosUseCase listarProductosUseCase) {
        this.crearProductoUseCase = crearProductoUseCase;
        this.buscarProductoUseCase = buscarProductoUseCase;
        this.listarProductosUseCase = listarProductosUseCase;
    }

    @Post
    public HttpResponse<ProductoResponse> crear(@Body @Valid CrearProductoRequest request) {
        ProductoResponse response = crearProductoUseCase.ejecutar(request.toCommand(tiendaIdActual()));
        return HttpResponse.status(HttpStatus.CREATED).body(response);
    }

    @Get
    public HttpResponse<List<ProductoResponse>> listar() {
        List<ProductoResponse> response = listarProductosUseCase.ejecutar(new ListarProductosCommand(tiendaIdActual()));
        return HttpResponse.ok(response);
    }

    @Get("/{id}")
    public HttpResponse<ProductoResponse> buscar(@PathVariable UUID id) {
        ProductoResponse response = buscarProductoUseCase.ejecutar(new BuscarProductoCommand(tiendaIdActual(), id));
        return HttpResponse.ok(response);
    }

    private UUID tiendaIdActual() {
        return TenantContext.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay tienda_id en el contexto. ¿Falta el JWT en la petición?"));
    }
}
