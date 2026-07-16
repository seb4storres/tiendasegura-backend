package co.tiendasegura.shared.infrastructure.web;

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
 * Red de seguridad transversal para validaciones de negocio expresadas como
 * IllegalArgumentException (compact constructors de Commands, factory
 * methods de entidades de dominio en auth/inventario/ventas) que no están
 * cubiertas por una excepción de dominio específica de ningún módulo.
 *
 * Sin este handler, Micronaut deja caer estas excepciones al manejador
 * genérico y responde 500 Internal Server Error en vez de 400 Bad Request,
 * a pesar de que el mensaje ya describe con precisión un error del cliente.
 */
@Singleton
@Produces
public class IllegalArgumentExceptionHandler
        implements ExceptionHandler<IllegalArgumentException, HttpResponse<IllegalArgumentExceptionHandler.ErrorResponse>> {

    private static final Logger log = LoggerFactory.getLogger(IllegalArgumentExceptionHandler.class);

    @Override
    public HttpResponse<ErrorResponse> handle(HttpRequest request, IllegalArgumentException exception) {
        log.warn("Solicitud inválida: {} → HTTP 400", exception.getMessage());

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.getCode(),
                "SOLICITUD_INVALIDA",
                exception.getMessage(),
                request.getPath(),
                Instant.now().toString()
        );

        return HttpResponse.status(HttpStatus.BAD_REQUEST).body(body);
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
