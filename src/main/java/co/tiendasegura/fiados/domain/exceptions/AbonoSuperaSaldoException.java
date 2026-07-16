package co.tiendasegura.fiados.domain.exceptions;

import java.math.BigDecimal;
import java.util.UUID;

public class AbonoSuperaSaldoException extends FiadosDomainException {

    public AbonoSuperaSaldoException(UUID clienteId, BigDecimal monto, BigDecimal saldoActual) {
        super(
                "El abono de %s supera el saldo pendiente del cliente '%s' (%s)"
                        .formatted(monto, clienteId, saldoActual),
                "FIADOS_ABONO_SUPERA_SALDO"
        );
    }
}
