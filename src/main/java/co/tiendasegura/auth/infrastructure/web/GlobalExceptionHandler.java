package co.tiendasegura.auth.infrastructure.web;

import co.tiendasegura.auth.domain.exceptions.AuthDomainException;
import co.tiendasegura.auth.domain.exceptions.CredencialesInvalidasException;
import co.tiendasegura.auth.domain.exceptions.EmailYaRegistradoException;
import co.tiendasegura.auth.domain.exceptions.TiendaInactivaException;
import co.tiendasegura.auth.domain.exceptions.UsuarioInactivoException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Captura TODAS las excepciones de dominio de auth y las convierte
 * en respuestas HTTP con el status code apropiado.
 *
 * Mapeo:
 *   - CredencialesInvalidasException → 401 Unauthorized
 *   - EmailYaRegistradoException     → 409 Conflict
 *   - TiendaInactivaException        → 403 Forbidden
 *   - UsuarioInactivoException       → 403 Forbidden
 *   - Cualquier otra AuthDomainException → 400 Bad Request
 */
@Singleton
@Produces
public class GlobalExceptionHandler
        implements ExceptionHandler<AuthDomainException, HttpResponse<GlobalExceptionHandler.ErrorResponse>> {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public HttpResponse<ErrorResponse> handle(HttpRequest request, AuthDomainException exception) {
        HttpStatus status = resolverStatus(exception);

        log.warn("Error de dominio [{}]: {} → HTTP {}",
                exception.getCodigoError(), exception.getMessage(), status.getCode());

        ErrorResponse body = new ErrorResponse(
                status.getCode(),
                exception.getCodigoError(),
                exception.getMessage(),
                request.getPath(),
                Instant.now().toString()
        );

        return HttpResponse.status(status).body(body);
    }

    private HttpStatus resolverStatus(AuthDomainException ex) {
        return switch (ex) {
            case CredencialesInvalidasException e -> HttpStatus.UNAUTHORIZED;
            case EmailYaRegistradoException e     -> HttpStatus.CONFLICT;
            case TiendaInactivaException e        -> HttpStatus.FORBIDDEN;
            case UsuarioInactivoException e       -> HttpStatus.FORBIDDEN;
            default                               -> HttpStatus.BAD_REQUEST;
        };
    }

    @Serdeable
    public record ErrorResponse(
            int status,
            String codigo,
            String mensaje,
            String path,
            String timestamp
    ) {}
}