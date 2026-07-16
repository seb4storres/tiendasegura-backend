package co.tiendasegura.inventario.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de producto del inventario. Siempre pertenece a una Tienda (tenant).
 * Java puro — cero dependencias de frameworks.
 */
public class Producto {

    private final UUID id;
    private final UUID tiendaId;
    private String codigoBarras;
    private String nombre;
    private String descripcion;
    private UUID categoriaId;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private int stock;
    private int stockMinimo;
    private boolean activo;
    private int version;
    private final Instant createdAt;
    private Instant updatedAt;

    // ── Constructor privado: toda creación pasa por factory methods ──

    private Producto(UUID id, UUID tiendaId, String codigoBarras, String nombre, String descripcion,
                     UUID categoriaId, BigDecimal precioCompra, BigDecimal precioVenta,
                     int stock, int stockMinimo, boolean activo, int version,
                     Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "El ID del producto es obligatorio");
        this.tiendaId = Objects.requireNonNull(tiendaId, "El tiendaId es obligatorio");
        this.codigoBarras = normalizarCodigoBarras(codigoBarras);
        this.nombre = validarNombre(nombre);
        this.descripcion = descripcion;
        this.categoriaId = categoriaId;
        this.precioCompra = precioCompra != null ? precioCompra : BigDecimal.ZERO;
        this.precioVenta = validarPrecioVenta(precioVenta);
        this.stock = validarStock(stock);
        this.stockMinimo = Math.max(stockMinimo, 0);
        this.activo = activo;
        this.version = version;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // ── Factory: crear producto nuevo ──

    public static Producto crear(UUID tiendaId, String nombre, String codigoBarras, String descripcion,
                                 UUID categoriaId, BigDecimal precioCompra, BigDecimal precioVenta,
                                 int stockInicial, int stockMinimo) {
        Instant ahora = Instant.now();
        return new Producto(
                UUID.randomUUID(),
                tiendaId,
                codigoBarras,
                nombre,
                descripcion,
                categoriaId,
                precioCompra,
                precioVenta,
                stockInicial,
                stockMinimo,
                true,   // Activo por defecto
                1,      // Version inicial (optimistic concurrency)
                ahora,
                ahora
        );
    }

    // ── Factory: reconstituir desde base de datos ──

    public static Producto reconstituir(UUID id, UUID tiendaId, String codigoBarras, String nombre,
                                        String descripcion, UUID categoriaId, BigDecimal precioCompra,
                                        BigDecimal precioVenta, int stock, int stockMinimo,
                                        boolean activo, int version, Instant createdAt, Instant updatedAt) {
        return new Producto(id, tiendaId, codigoBarras, nombre, descripcion, categoriaId,
                precioCompra, precioVenta, stock, stockMinimo, activo, version, createdAt, updatedAt);
    }

    // ── Comportamiento de dominio ──

    public void desactivar() {
        this.activo = false;
        this.updatedAt = Instant.now();
    }

    public void activar() {
        this.activo = true;
        this.updatedAt = Instant.now();
    }

    public boolean tieneStockBajo() {
        return stock <= stockMinimo;
    }

    // ── Validaciones internas ──

    private static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (nombre.length() > 200) {
            throw new IllegalArgumentException("El nombre no puede superar 200 caracteres");
        }
        return nombre.strip();
    }

    private static BigDecimal validarPrecioVenta(BigDecimal precioVenta) {
        if (precioVenta == null || precioVenta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor a cero");
        }
        return precioVenta;
    }

    private static int validarStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        return stock;
    }

    private static String normalizarCodigoBarras(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.isBlank()) {
            return null;
        }
        return codigoBarras.strip();
    }

    // ── Getters (sin setters públicos — estado se modifica solo por métodos de dominio) ──

    public UUID getId() { return id; }
    public UUID getTiendaId() { return tiendaId; }
    public String getCodigoBarras() { return codigoBarras; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public UUID getCategoriaId() { return categoriaId; }
    public BigDecimal getPrecioCompra() { return precioCompra; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public int getStock() { return stock; }
    public int getStockMinimo() { return stockMinimo; }
    public boolean isActivo() { return activo; }
    public int getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Producto{id=%s, nombre='%s', codigoBarras='%s', tiendaId=%s}".formatted(id, nombre, codigoBarras, tiendaId);
    }
}
