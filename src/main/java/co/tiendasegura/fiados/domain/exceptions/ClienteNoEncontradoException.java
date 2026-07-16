package co.tiendasegura.fiados.domain.exceptions;

import java.util.UUID;

public class ClienteNoEncontradoException extends FiadosDomainException {

    public ClienteNoEncontradoException(UUID id) {
        super(
                "No se encontró el cliente con ID '%s'".formatted(id),
                "FIADOS_CLIENTE_NO_ENCONTRADO"
        );
    }
}
