package co.tiendasegura.fiados.domain.exceptions;

import java.util.UUID;

/**
 * Se lanza cuando el UPDATE condicionado por `version` no afecta ninguna
 * fila — es decir, otra operación (otro abono, otra deuda) modificó el
 * saldo del cliente entre la lectura y esta escritura (optimistic
 * concurrency control). El caller debe reintentar con el estado fresco.
 */
public class ClienteModificadoConcurrentementeException extends FiadosDomainException {

    public ClienteModificadoConcurrentementeException(UUID clienteId) {
        super(
                "El cliente '%s' fue modificado por otra operación mientras se procesaba esta solicitud. Reintenta."
                        .formatted(clienteId),
                "FIADOS_CONFLICTO_CONCURRENCIA"
        );
    }
}
