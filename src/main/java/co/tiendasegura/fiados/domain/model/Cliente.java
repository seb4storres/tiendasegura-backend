package co.tiendasegura.fiados.domain.model;

import co.tiendasegura.fiados.domain.exceptions.AbonoSuperaSaldoException;
import co.tiendasegura.fiados.domain.exceptions.ClienteBloqueadoException;
import co.tiendasegura.fiados.domain.exceptions.LimiteCreditoExcedidoException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad del cliente a quien se le puede fiar (vender a crédito).
 *
 * saldoActual es un SALDO CORRIDO — no hay un registro por venta
 * individual: cada deuda lo aumenta, cada abono lo reduce. El propio
 * Cliente protege la invariante "no fiar por encima del límite de
 * crédito" y "no aceptar abonos por encima de lo que debe" — a diferencia
 * de las validaciones de stock en Inventario/Ventas (que dependen de leer
 * OTRO agregado), aquí toda la información necesaria ya vive en esta
 * misma entidad, así que la regla vive aquí, no en el servicio de
 * aplicación.
 */
public class Cliente {

    private final UUID id;
    private final UUID tiendaId;
    private String nombre;
    private String telefono;
    private String cedula;
    private String direccion;
    private BigDecimal limiteCredito;
    private BigDecimal saldoActual;
    private EstadoCliente estado;
    private int version;
    private final Instant createdAt;
    private Instant updatedAt;

    // ── Constructor privado: toda creación pasa por factory methods ──

    private Cliente(UUID id, UUID tiendaId, String nombre, String telefono, String cedula, String direccion,
                    BigDecimal limiteCredito, BigDecimal saldoActual, EstadoCliente estado, int version,
                    Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "El ID del cliente es obligatorio");
        this.tiendaId = Objects.requireNonNull(tiendaId, "El tiendaId es obligatorio");
        this.nombre = validarNombre(nombre);
        this.telefono = telefono;
        this.cedula = cedula;
        this.direccion = direccion;
        this.limiteCredito = validarLimiteCredito(limiteCredito);
        this.saldoActual = Objects.requireNonNull(saldoActual, "El saldo actual es obligatorio");
        this.estado = Objects.requireNonNull(estado, "El estado es obligatorio");
        this.version = version;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // ── Factory: crear cliente nuevo (arranca en ACTIVO, saldo cero) ──

    public static Cliente crear(UUID tiendaId, String nombre, String telefono, String cedula,
                                String direccion, BigDecimal limiteCredito) {
        Instant ahora = Instant.now();
        return new Cliente(
                UUID.randomUUID(), tiendaId, nombre, telefono, cedula, direccion,
                limiteCredito, BigDecimal.ZERO, EstadoCliente.ACTIVO, 1, ahora, ahora
        );
    }

    // ── Factory: reconstituir desde base de datos ──

    public static Cliente reconstituir(UUID id, UUID tiendaId, String nombre, String telefono, String cedula,
                                       String direccion, BigDecimal limiteCredito, BigDecimal saldoActual,
                                       EstadoCliente estado, int version, Instant createdAt, Instant updatedAt) {
        return new Cliente(id, tiendaId, nombre, telefono, cedula, direccion,
                limiteCredito, saldoActual, estado, version, createdAt, updatedAt);
    }

    // ── Comportamiento de dominio ──

    /**
     * Aumenta el saldo por una nueva deuda (venta a crédito). Rechaza la
     * operación si el cliente está bloqueado o si el nuevo saldo superaría
     * el límite de crédito.
     */
    public void registrarDeuda(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de la deuda debe ser mayor a cero");
        }
        if (estado == EstadoCliente.BLOQUEADO) {
            throw new ClienteBloqueadoException(id);
        }
        BigDecimal nuevoSaldo = saldoActual.add(monto);
        if (nuevoSaldo.compareTo(limiteCredito) > 0) {
            throw new LimiteCreditoExcedidoException(id, monto, saldoActual, limiteCredito);
        }
        this.saldoActual = nuevoSaldo;
        this.version++;
        this.updatedAt = Instant.now();
    }

    /**
     * Reduce el saldo por un abono (pago parcial o total). No permite
     * abonar más de lo que el cliente debe.
     */
    public void registrarAbono(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del abono debe ser mayor a cero");
        }
        if (monto.compareTo(saldoActual) > 0) {
            throw new AbonoSuperaSaldoException(id, monto, saldoActual);
        }
        this.saldoActual = this.saldoActual.subtract(monto);
        this.version++;
        this.updatedAt = Instant.now();
    }

    public void bloquear() {
        this.estado = EstadoCliente.BLOQUEADO;
        this.updatedAt = Instant.now();
    }

    public void activar() {
        this.estado = EstadoCliente.ACTIVO;
        this.updatedAt = Instant.now();
    }

    // ── Validaciones internas ──

    private static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        if (nombre.length() > 200) {
            throw new IllegalArgumentException("El nombre no puede superar 200 caracteres");
        }
        return nombre.strip();
    }

    private static BigDecimal validarLimiteCredito(BigDecimal limiteCredito) {
        if (limiteCredito == null || limiteCredito.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El límite de crédito no puede ser negativo");
        }
        return limiteCredito;
    }

    // ── Getters (sin setters públicos — estado se modifica solo por métodos de dominio) ──

    public UUID getId() { return id; }
    public UUID getTiendaId() { return tiendaId; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getCedula() { return cedula; }
    public String getDireccion() { return direccion; }
    public BigDecimal getLimiteCredito() { return limiteCredito; }
    public BigDecimal getSaldoActual() { return saldoActual; }
    public EstadoCliente getEstado() { return estado; }
    public int getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Cliente{id=%s, nombre='%s', saldoActual=%s, limiteCredito=%s, estado=%s}"
                .formatted(id, nombre, saldoActual, limiteCredito, estado);
    }
}
