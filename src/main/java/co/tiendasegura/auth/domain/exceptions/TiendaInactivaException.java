package co.tiendasegura.auth.domain.exceptions;

import java.util.UUID;

public class TiendaInactivaException extends AuthDomainException {

    public TiendaInactivaException(UUID tiendaId) {
        super(
                "La tienda con ID '%s' se encuentra inactiva".formatted(tiendaId),
                "AUTH_TIENDA_INACTIVA"
        );
    }
}