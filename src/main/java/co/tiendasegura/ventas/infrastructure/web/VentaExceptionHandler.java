package co.tiendasegura.ventas.infrastructure.web;

import co.tiendasegura.ventas.domain.exceptions.ProductoNoDisponibleException;
import co.tiendasegura.ventas.domain.exceptions.StockInsuficienteException;
import co.tiendasegura.ventas.domain.exceptions.VentaDomainException;
import co.tiendasegura.ventas.domain.exceptions.VentaDuplicadaException;
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
 * Captura TODAS las excepciones de dominio de ventas y las convierte
 * en respuestas HTTP con el status code apropiado.
 *
 * Mapeo:
 *   - ProductoNoDisponibleException → 404 Not Found
 *   - StockInsuficienteException    → 409 Conflict
 *   - VentaDuplicadaException       → 409 Conflict
 *   - Cualquier otra VentaDomainException → 400 Bad Request
 */
@Singleton
@Produces
public class VentaExceptionHandler
        implements ExceptionHandler<VentaDomainException, HttpResponse<VentaExceptionHandler.ErrorResponse>> {

    private static final Logger log = LoggerFactory.getLogger(VentaExceptionHandler.class);

    @Override
    public HttpResponse<ErrorResponse> handle(HttpRequest request, VentaDomainException exception) {
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

    private HttpStatus resolverStatus(VentaDomainException ex) {
        return switch (ex) {
            case ProductoNoDisponibleException e -> HttpStatus.NOT_FOUND;
            case StockInsuficienteException e    -> HttpStatus.CONFLICT;
            case VentaDuplicadaException e        -> HttpStatus.CONFLICT;
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
