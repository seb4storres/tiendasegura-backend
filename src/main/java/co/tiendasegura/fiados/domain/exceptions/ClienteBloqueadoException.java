package co.tiendasegura.fiados.domain.exceptions;

import java.util.UUID;

public class ClienteBloqueadoException extends FiadosDomainException {

    public ClienteBloqueadoException(UUID clienteId) {
        super(
                "El cliente '%s' está bloqueado y no puede adquirir más crédito".formatted(clienteId),
                "FIADOS_CLIENTE_BLOQUEADO"
        );
    }
}
