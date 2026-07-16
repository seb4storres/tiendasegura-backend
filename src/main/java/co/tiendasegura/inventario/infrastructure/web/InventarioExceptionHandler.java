package co.tiendasegura.inventario.infrastructure.web;

import co.tiendasegura.inventario.domain.exceptions.CodigoBarrasDuplicadoException;
import co.tiendasegura.inventario.domain.exceptions.InventarioDomainException;
import co.tiendasegura.inventario.domain.exceptions.ProductoNoEncontradoException;
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
 * Captura TODAS las excepciones de dominio de inventario y las convierte
 * en respuestas HTTP con el status code apropiado.
 *
 * Mapeo:
 *   - ProductoNoEncontradoException  → 404 Not Found
 *   - CodigoBarrasDuplicadoException → 409 Conflict
 *   - Cualquier otra InventarioDomainException → 400 Bad Request
 */
@Singleton
@Produces
public class InventarioExceptionHandler
        implements ExceptionHandler<InventarioDomainException, HttpResponse<InventarioExceptionHandler.ErrorResponse>> {

    private static final Logger log = LoggerFactory.getLogger(InventarioExceptionHandler.class);

    @Override
    public HttpResponse<ErrorResponse> handle(HttpRequest request, InventarioDomainException exception) {
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

    private HttpStatus resolverStatus(InventarioDomainException ex) {
        return switch (ex) {
            case ProductoNoEncontradoException e  -> HttpStatus.NOT_FOUND;
            case CodigoBarrasDuplicadoException e -> HttpStatus.CONFLICT;
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
