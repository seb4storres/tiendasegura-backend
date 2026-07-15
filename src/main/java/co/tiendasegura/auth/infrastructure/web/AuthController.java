package co.tiendasegura.auth.infrastructure.web;

import co.tiendasegura.auth.application.dto.AuthResponse;
import co.tiendasegura.auth.application.ports.in.LoginUseCase;
import co.tiendasegura.auth.application.ports.in.RegistrarTiendaUseCase;
import co.tiendasegura.auth.infrastructure.web.dto.LoginRequest;
import co.tiendasegura.auth.infrastructure.web.dto.RegistroRequest;
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

/**
 * Adaptador de entrada HTTP para autenticación.
 *
 * - @Secured(IS_ANONYMOUS): ambos endpoints son públicos (no requieren JWT).
 * - @ExecuteOn(BLOCKING): las operaciones JDBC bloquean; se ejecutan
 *   en el thread pool de I/O, no en el event loop de Netty.
 * - @Validated: activa la validación de Jakarta en los @Body.
 *
 * El controller es un "adaptador fino": solo convierte DTO HTTP → Command,
 * delega al caso de uso, y convierte el resultado a HttpResponse.
 */
@Controller("/auth")
@Secured(SecurityRule.IS_ANONYMOUS)
@ExecuteOn(TaskExecutors.BLOCKING)
@Validated
public class AuthController {

    private final RegistrarTiendaUseCase registrarTiendaUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(RegistrarTiendaUseCase registrarTiendaUseCase,
                          LoginUseCase loginUseCase) {
        this.registrarTiendaUseCase = registrarTiendaUseCase;
        this.loginUseCase = loginUseCase;
    }

    @Post("/registro")
    public HttpResponse<AuthResponse> registrar(@Body @Valid RegistroRequest request) {
        AuthResponse response = registrarTiendaUseCase.ejecutar(request.toCommand());
        return HttpResponse.status(HttpStatus.CREATED).body(response);
    }

    @Post("/login")
    public HttpResponse<AuthResponse> login(@Body @Valid LoginRequest request) {
        AuthResponse response = loginUseCase.ejecutar(request.toCommand());
        return HttpResponse.ok(response);
    }
}