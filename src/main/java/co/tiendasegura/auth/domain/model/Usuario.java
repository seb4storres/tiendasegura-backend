package co.tiendasegura.auth.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de usuario del sistema. Siempre pertenece a una Tienda (tenant).
 * El passwordHash se almacena ya encriptado — la entidad no conoce
 * la implementación de hashing (PasswordEncoderPort vive como puerto).
 */
public class Usuario {

    private final UUID id;
    private final UUID tiendaId;
    private String email;
    private String passwordHash;
    private String nombre;
    private Rol rol;
    private boolean activo;
    private final Instant createdAt;
    private Instant updatedAt;

    // ── Constructor privado ──

    private Usuario(UUID id, UUID tiendaId, String email, String passwordHash,
                    String nombre, Rol rol, boolean activo,
                    Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "El ID del usuario es obligatorio");
        this.tiendaId = Objects.requireNonNull(tiendaId, "El tiendaId es obligatorio");
        this.email = validarEmail(email);
        this.passwordHash = Objects.requireNonNull(passwordHash, "El hash del password es obligatorio");
        this.nombre = validarNombre(nombre);
        this.rol = Objects.requireNonNull(rol, "El rol es obligatorio");
        this.activo = activo;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // ── Factory: crear usuario nuevo (el password ya viene hasheado desde el servicio) ──

    public static Usuario crear(UUID tiendaId, String email, String passwordHash,
                                String nombre, Rol rol) {
        Instant ahora = Instant.now();
        return new Usuario(
                UUID.randomUUID(),
                tiendaId,
                email,
                passwordHash,
                nombre,
                rol,
                true,
                ahora,
                ahora
        );
    }

    // ── Factory: crear el usuario ADMIN inicial al registrar una tienda ──

    public static Usuario crearAdmin(UUID tiendaId, String email,
                                     String passwordHash, String nombre) {
        return crear(tiendaId, email, passwordHash, nombre, Rol.ADMIN);
    }

    // ── Factory: reconstituir desde base de datos ──

    public static Usuario reconstituir(UUID id, UUID tiendaId, String email,
                                       String passwordHash, String nombre,
                                       Rol rol, boolean activo,
                                       Instant createdAt, Instant updatedAt) {
        return new Usuario(id, tiendaId, email, passwordHash, nombre, rol, activo, createdAt, updatedAt);
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

    public void cambiarRol(Rol nuevoRol) {
        Objects.requireNonNull(nuevoRol, "El rol no puede ser nulo");
        this.rol = nuevoRol;
        this.updatedAt = Instant.now();
    }

    public void actualizarPasswordHash(String nuevoHash) {
        this.passwordHash = Objects.requireNonNull(nuevoHash, "El hash no puede ser nulo");
        this.updatedAt = Instant.now();
    }

    public boolean esAdmin() {
        return this.rol.esAdmin();
    }

    // ── Validaciones internas ──

    private static String validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        String emailLimpio = email.strip().toLowerCase();
        // Validación básica de formato — la validación exhaustiva se delega a Jakarta Validation en el DTO
        if (!emailLimpio.contains("@") || !emailLimpio.contains(".")) {
            throw new IllegalArgumentException("Formato de email inválido: " + email);
        }
        if (emailLimpio.length() > 255) {
            throw new IllegalArgumentException("El email no puede superar 255 caracteres");
        }
        return emailLimpio;
    }

    private static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del usuario es obligatorio");
        }
        if (nombre.length() > 150) {
            throw new IllegalArgumentException("El nombre no puede superar 150 caracteres");
        }
        return nombre.strip();
    }

    // ── Getters ──

    public UUID getId() { return id; }
    public UUID getTiendaId() { return tiendaId; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getNombre() { return nombre; }
    public Rol getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Usuario{id=%s, email='%s', rol=%s, tiendaId=%s}".formatted(id, email, rol, tiendaId);
    }
}