package co.tiendasegura.ventas.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Línea de una venta. Entidad hija dentro del agregado Venta — no se
 * persiste ni se referencia de forma independiente fuera de una venta.
 *
 * precioUnitarioSnapshot es INMUTABLE por diseño: se copia del precio del
 * producto en el momento de la venta y nunca se vuelve a leer desde
 * Inventario. Si el precio del producto cambia después, esta venta ya
 * registrada no se ve afectada — es un snapshot histórico.
 */
public class DetalleVenta {

    private final UUID id;
    private final UUID ventaId;
    private final UUID productoId;
    private final String nombreProductoSnapshot;
    private final int cantidad;
    private final BigDecimal precioUnitarioSnapshot;
    private final BigDecimal subtotal;
    private final Instant createdAt;

    // ── Constructor privado: toda creación pasa por factory methods ──

    private DetalleVenta(UUID id, UUID ventaId, UUID productoId, String nombreProductoSnapshot,
                         int cantidad, BigDecimal precioUnitarioSnapshot, BigDecimal subtotal,
                         Instant createdAt) {
        this.id = Objects.requireNonNull(id, "El ID del detalle es obligatorio");
        this.ventaId = Objects.requireNonNull(ventaId, "El ventaId es obligatorio");
        this.productoId = Objects.requireNonNull(productoId, "El productoId es obligatorio");
        this.nombreProductoSnapshot = Objects.requireNonNull(nombreProductoSnapshot,
                "El nombre del producto es obligatorio");
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        this.cantidad = cantidad;
        this.precioUnitarioSnapshot = Objects.requireNonNull(precioUnitarioSnapshot,
                "El precio unitario es obligatorio");
        this.subtotal = Objects.requireNonNull(subtotal, "El subtotal es obligatorio");
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    // ── Factory: crear detalle nuevo (el subtotal se calcula aquí, no lo decide el caller) ──

    public static DetalleVenta crear(UUID ventaId, UUID productoId, String nombreProductoSnapshot,
                                     int cantidad, BigDecimal precioUnitarioSnapshot) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (precioUnitarioSnapshot == null || precioUnitarioSnapshot.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor a cero");
        }
        BigDecimal subtotal = precioUnitarioSnapshot.multiply(BigDecimal.valueOf(cantidad));
        return new DetalleVenta(
                UUID.randomUUID(), ventaId, productoId, nombreProductoSnapshot,
                cantidad, precioUnitarioSnapshot, subtotal, Instant.now()
        );
    }

    // ── Factory: reconstituir desde base de datos ──

    public static DetalleVenta reconstituir(UUID id, UUID ventaId, UUID productoId, String nombreProductoSnapshot,
                                            int cantidad, BigDecimal precioUnitarioSnapshot, BigDecimal subtotal,
                                            Instant createdAt) {
        return new DetalleVenta(id, ventaId, productoId, nombreProductoSnapshot,
                cantidad, precioUnitarioSnapshot, subtotal, createdAt);
    }

    // ── Getters ──

    public UUID getId() { return id; }
    public UUID getVentaId() { return ventaId; }
    public UUID getProductoId() { return productoId; }
    public String getNombreProductoSnapshot() { return nombreProductoSnapshot; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitarioSnapshot() { return precioUnitarioSnapshot; }
    public BigDecimal getSubtotal() { return subtotal; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetalleVenta that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "DetalleVenta{id=%s, productoId=%s, cantidad=%d, subtotal=%s}"
                .formatted(id, productoId, cantidad, subtotal);
    }
}
