package co.tiendasegura.ventas.domain.exceptions;

import java.util.UUID;

/**
 * Se lanza cuando el INSERT de la venta choca contra la PRIMARY KEY de
 * `ventas.id` — es decir, dos solicitudes con la misma clave de idempotencia
 * llegaron casi al mismo tiempo y ambas pasaron la verificación previa de
 * "¿ya existe?" antes de que cualquiera de las dos confirmara. La transacción
 * de este intento se revierte por completo (incluyendo el stock ya
 * descontado), así que un reintento del cliente con el mismo ID encontrará
 * la venta de la solicitud que sí ganó la carrera.
 */
public class VentaDuplicadaException extends VentaDomainException {

    public VentaDuplicadaException(UUID ventaId) {
        super(
                "Ya existe una venta en proceso con el ID '%s' (posible solicitud duplicada). Reintenta la consulta."
                        .formatted(ventaId),
                "VENTA_DUPLICADA"
        );
    }
}
