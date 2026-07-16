package co.tiendasegura.fiados.application.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * saldoRestante es el saldo del cliente DESPUÉS de aplicar este abono —
 * el cajero lo necesita de inmediato, sin una segunda consulta.
 */
@Serdeable
public record AbonoResponse(
        UUID id,
        UUID clienteId,
        BigDecimal monto,
        String metodoPago,
        String nota,
        Instant fecha,
        BigDecimal saldoRestante
) {}
