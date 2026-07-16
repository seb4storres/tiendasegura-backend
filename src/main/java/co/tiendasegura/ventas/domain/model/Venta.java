package co.tiendasegura.ventas.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root de una venta. Contiene su lista de DetalleVenta — no existen
 * detalles fuera del ciclo de vida de una venta.
 *
 * El id NO se genera aquí: lo recibe desde afuera (clave de idempotencia
 * generada por el cliente). subtotal/total/cambio SÍ se calculan aquí, nunca
 * los decide el caller — son invariantes del agregado.
 */
public class Venta {

    private final UUID id;
    private final UUID tiendaId;
    private final UUID cajeroId;
    private final UUID clienteId;
    private final Instant fecha;
    private final BigDecimal subtotal;
    private final BigDecimal descuento;
    private final BigDecimal total;
    private final MetodoPago metodoPago;
    private final BigDecimal montoRecibido;
    private final BigDecimal cambio;
    private final EstadoVenta estado;
    private final String notas;
    private final Instant createdAt;
    private final List<DetalleVenta> detalles;

    // ── Constructor privado: toda creación pasa por factory methods ──

    private Venta(UUID id, UUID tiendaId, UUID cajeroId, UUID clienteId, Instant fecha,
                  BigDecimal subtotal, BigDecimal descuento, BigDecimal total,
                  MetodoPago metodoPago, BigDecimal montoRecibido, BigDecimal cambio,
                  EstadoVenta estado, String notas, Instant createdAt, List<DetalleVenta> detalles) {
        this.id = Objects.requireNonNull(id, "El ID de la venta es obligatorio");
        this.tiendaId = Objects.requireNonNull(tiendaId, "El tiendaId es obligatorio");
        this.cajeroId = Objects.requireNonNull(cajeroId, "El cajeroId es obligatorio");
        this.clienteId = clienteId;
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria");
        this.subtotal = Objects.requireNonNull(subtotal, "El subtotal es obligatorio");
        this.descuento = Objects.requireNonNull(descuento, "El descuento es obligatorio");
        this.total = Objects.requireNonNull(total, "El total es obligatorio");
        this.metodoPago = Objects.requireNonNull(metodoPago, "El método de pago es obligatorio");
        this.montoRecibido = montoRecibido;
        this.cambio = cambio;
        this.estado = Objects.requireNonNull(estado, "El estado es obligatorio");
        this.notas = notas;
        this.createdAt = Objects.requireNonNull(createdAt);
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("Una venta debe tener al menos un detalle");
        }
        this.detalles = List.copyOf(detalles);
    }

    // ── Factory: registrar venta nueva ──
    // El id lo genera el CLIENTE (clave de idempotencia) — no se genera aquí.

    public static Venta crear(UUID id, UUID tiendaId, UUID cajeroId, UUID clienteId,
                              List<DetalleVenta> detalles, BigDecimal descuento,
                              MetodoPago metodoPago, BigDecimal montoRecibido, String notas) {
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("Una venta debe tener al menos un detalle");
        }
        if (metodoPago == MetodoPago.FIADO && clienteId == null) {
            throw new IllegalArgumentException("Una venta a crédito (FIADO) requiere un cliente");
        }

        BigDecimal subtotal = detalles.stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal descuentoAplicado = descuento != null ? descuento : BigDecimal.ZERO;
        if (descuentoAplicado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El descuento no puede ser negativo");
        }
        if (descuentoAplicado.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("El descuento no puede superar el subtotal");
        }

        BigDecimal total = subtotal.subtract(descuentoAplicado);

        BigDecimal cambio = null;
        if (metodoPago == MetodoPago.EFECTIVO) {
            if (montoRecibido == null || montoRecibido.compareTo(total) < 0) {
                throw new IllegalArgumentException("El monto recibido es insuficiente para cubrir el total");
            }
            cambio = montoRecibido.subtract(total);
        }

        Instant ahora = Instant.now();
        return new Venta(
                id, tiendaId, cajeroId, clienteId, ahora,
                subtotal, descuentoAplicado, total,
                metodoPago, montoRecibido, cambio,
                EstadoVenta.COMPLETADA, notas, ahora, detalles
        );
    }

    // ── Factory: reconstituir desde base de datos ──

    public static Venta reconstituir(UUID id, UUID tiendaId, UUID cajeroId, UUID clienteId, Instant fecha,
                                     BigDecimal subtotal, BigDecimal descuento, BigDecimal total,
                                     MetodoPago metodoPago, BigDecimal montoRecibido, BigDecimal cambio,
                                     EstadoVenta estado, String notas, Instant createdAt,
                                     List<DetalleVenta> detalles) {
        return new Venta(id, tiendaId, cajeroId, clienteId, fecha, subtotal, descuento, total,
                metodoPago, montoRecibido, cambio, estado, notas, createdAt, detalles);
    }

    // ── Getters ──

    public UUID getId() { return id; }
    public UUID getTiendaId() { return tiendaId; }
    public UUID getCajeroId() { return cajeroId; }
    public UUID getClienteId() { return clienteId; }
    public Instant getFecha() { return fecha; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDescuento() { return descuento; }
    public BigDecimal getTotal() { return total; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public BigDecimal getMontoRecibido() { return montoRecibido; }
    public BigDecimal getCambio() { return cambio; }
    public EstadoVenta getEstado() { return estado; }
    public String getNotas() { return notas; }
    public Instant getCreatedAt() { return createdAt; }
    public List<DetalleVenta> getDetalles() { return detalles; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Venta that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Venta{id=%s, tiendaId=%s, total=%s, estado=%s, detalles=%d}"
                .formatted(id, tiendaId, total, estado, detalles.size());
    }
}
