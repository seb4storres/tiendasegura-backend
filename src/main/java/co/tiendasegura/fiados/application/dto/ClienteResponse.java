package co.tiendasegura.fiados.application.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.util.UUID;

@Serdeable
public record ClienteResponse(
        UUID id,
        UUID tiendaId,
        String nombre,
        String telefono,
        String cedula,
        String direccion,
        BigDecimal limiteCredito,
        BigDecimal saldoActual,
        String estado,
        int version
) {}
