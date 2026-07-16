package co.tiendasegura.fiados.domain.exceptions;

import java.math.BigDecimal;
import java.util.UUID;

public class LimiteCreditoExcedidoException extends FiadosDomainException {

    public LimiteCreditoExcedidoException(UUID clienteId, BigDecimal monto,
                                          BigDecimal saldoActual, BigDecimal limiteCredito) {
        super(
                "El cliente '%s' no puede fiar %s: saldo actual %s + esta deuda superaría el límite de crédito de %s"
                        .formatted(clienteId, monto, saldoActual, limiteCredito),
                "FIADOS_LIMITE_CREDITO_EXCEDIDO"
        );
    }
}
