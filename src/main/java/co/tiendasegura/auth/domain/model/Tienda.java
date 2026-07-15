package co.tiendasegura.auth.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad raíz del tenant. Cada minimercado es una Tienda.
 * Java puro — cero dependencias de frameworks.
 */
public class Tienda {

    private final UUID id;
    private String nombre;
    private String nit;
    private String direccion;
    private String telefono;
    private Plan plan;
    private boolean activa;
    private final Instant createdAt;
    private Instant updatedAt;

    // ── Constructor privado: toda creación pasa por factory methods ──

    private Tienda(UUID id, String nombre, String nit, String direccion,
                   String telefono, Plan plan, boolean activa,
                   Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "El ID de la tienda es obligatorio");
        this.nombre = validarNombre(nombre);
        this.nit = nit;
        this.direccion = direccion;
        this.telefono = telefono;
        this.plan = Objects.requireNonNull(plan, "El plan es obligatorio");
        this.activa = activa;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // ── Factory: crear tienda nueva (registro) ──

    public static Tienda crear(String nombre, String nit, String direccion, String telefono) {
        Instant ahora = Instant.now();
        return new Tienda(
                UUID.randomUUID(),
                nombre,
                nit,
                direccion,
                telefono,
                Plan.BASICO,    // Toda tienda nueva arranca en plan BASICO
                true,           // Activa por defecto
                ahora,
                ahora
        );
    }

    // ── Factory: reconstituir desde base de datos ──

    public static Tienda reconstituir(UUID id, String nombre, String nit,
                                      String direccion, String telefono,
                                      Plan plan, boolean activa,
                                      Instant createdAt, Instant updatedAt) {
        return new Tienda(id, nombre, nit, direccion, telefono, plan, activa, createdAt, updatedAt);
    }

    // ── Comportamiento de dominio ──

    public void desactivar() {
        this.activa = false;
        this.updatedAt = Instant.now();
    }

    public void activar() {
        this.activa = true;
        this.updatedAt = Instant.now();
    }

    public void cambiarPlan(Plan nuevoPlan) {
        Objects.requireNonNull(nuevoPlan, "El plan no puede ser nulo");
        this.plan = nuevoPlan;
        this.updatedAt = Instant.now();
    }

    // ── Validaciones internas ──

    private static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la tienda es obligatorio");
        }
        if (nombre.length() > 150) {
            throw new IllegalArgumentException("El nombre no puede superar 150 caracteres");
        }
        return nombre.strip();
    }

    // ── Getters (sin setters públicos — estado se modifica solo por métodos de dominio) ──

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getNit() { return nit; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public Plan getPlan() { return plan; }
    public boolean isActiva() { return activa; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tienda that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Tienda{id=%s, nombre='%s', plan=%s, activa=%s}".formatted(id, nombre, plan, activa);
    }
}