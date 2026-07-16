package co.tiendasegura.fiados.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Registro de un pago (parcial o total) que reduce el saldo de un Cliente.
 * En el modelo simple de fiados, un Abono aplica directo contra el saldo
 * corrido del cliente — no contra una deuda individual.
 */
public class Abono {

    private final UUID id;
    private final UUID tiendaId;
    private final UUID clienteId;
    private final BigDecimal monto;
    private final String metodoPago;
    private final String nota;
    private final Instant fecha;
    private final Instant createdAt;

    // ── Constructor privado: toda creación pasa por factory methods ──

    private Abono(UUID id, UUID tiendaId, UUID clienteId, BigDecimal monto, String metodoPago,
                  String nota, Instant fecha, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "El ID del abono es obligatorio");
        this.tiendaId = Objects.requireNonNull(tiendaId, "El tiendaId es obligatorio");
        this.clienteId = Objects.requireNonNull(clienteId, "El clienteId es obligatorio");
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del abono debe ser mayor a cero");
        }
        this.monto = monto;
        this.metodoPago = Objects.requireNonNull(metodoPago, "El método de pago es obligatorio");
        this.nota = nota;
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria");
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    // ── Factory: crear abono nuevo ──

    public static Abono crear(UUID tiendaId, UUID clienteId, BigDecimal monto, String metodoPago, String nota) {
        Instant ahora = Instant.now();
        String metodoPagoFinal = (metodoPago == null || metodoPago.isBlank()) ? "EFECTIVO" : metodoPago.strip();
        return new Abono(UUID.randomUUID(), tiendaId, clienteId, monto, metodoPagoFinal, nota, ahora, ahora);
    }

    // ── Factory: reconstituir desde base de datos ──

    public static Abono reconstituir(UUID id, UUID tiendaId, UUID clienteId, BigDecimal monto,
                                     String metodoPago, String nota, Instant fecha, Instant createdAt) {
        return new Abono(id, tiendaId, clienteId, monto, metodoPago, nota, fecha, createdAt);
    }

    // ── Getters ──

    public UUID getId() { return id; }
    public UUID getTiendaId() { return tiendaId; }
    public UUID getClienteId() { return clienteId; }
    public BigDecimal getMonto() { return monto; }
    public String getMetodoPago() { return metodoPago; }
    public String getNota() { return nota; }
    public Instant getFecha() { return fecha; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Abono that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Abono{id=%s, clienteId=%s, monto=%s}".formatted(id, clienteId, monto);
    }
}
