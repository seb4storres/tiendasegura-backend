package co.tiendasegura.fiados.infrastructure.web;

import co.tiendasegura.fiados.domain.exceptions.AbonoSuperaSaldoException;
import co.tiendasegura.fiados.domain.exceptions.ClienteBloqueadoException;
import co.tiendasegura.fiados.domain.exceptions.ClienteModificadoConcurrentementeException;
import co.tiendasegura.fiados.domain.exceptions.ClienteNoEncontradoException;
import co.tiendasegura.fiados.domain.exceptions.FiadosDomainException;
import co.tiendasegura.fiados.domain.exceptions.LimiteCreditoExcedidoException;
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
 * Captura TODAS las excepciones de dominio de fiados y las convierte
 * en respuestas HTTP con el status code apropiado.
 *
 * Mapeo:
 *   - ClienteNoEncontradoException              → 404 Not Found
 *   - ClienteBloqueadoException                 → 403 Forbidden
 *   - LimiteCreditoExcedidoException             → 409 Conflict
 *   - AbonoSuperaSaldoException                  → 409 Conflict
 *   - ClienteModificadoConcurrentementeException → 409 Conflict
 *   - Cualquier otra FiadosDomainException → 400 Bad Request
 */
@Singleton
@Produces
public class FiadosExceptionHandler
        implements ExceptionHandler<FiadosDomainException, HttpResponse<FiadosExceptionHandler.ErrorResponse>> {

    private static final Logger log = LoggerFactory.getLogger(FiadosExceptionHandler.class);

    @Override
    public HttpResponse<ErrorResponse> handle(HttpRequest request, FiadosDomainException exception) {
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

    private HttpStatus resolverStatus(FiadosDomainException ex) {
        return switch (ex) {
            case ClienteNoEncontradoException e -> HttpStatus.NOT_FOUND;
            case ClienteBloqueadoException e -> HttpStatus.FORBIDDEN;
            case LimiteCreditoExcedidoException e -> HttpStatus.CONFLICT;
            case AbonoSuperaSaldoException e -> HttpStatus.CONFLICT;
            case ClienteModificadoConcurrentementeException e -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
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
