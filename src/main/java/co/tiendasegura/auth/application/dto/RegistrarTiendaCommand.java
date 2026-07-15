package co.tiendasegura.auth.application.dto;

/**
 * Command inmutable (Record de Java 21) para registrar una tienda nueva.
 * Incluye los datos de la tienda y del usuario administrador inicial.
 * Las validaciones de formato (@Email, @NotBlank) se aplican
 * en el DTO del controller HTTP — aquí el record es un contenedor limpio.
 */
public record RegistrarTiendaCommand(
        // Datos de la tienda
        String nombreTienda,
        String nit,
        String direccion,
        String telefono,

        // Datos del usuario administrador
        String emailAdmin,
        String passwordAdmin,
        String nombreAdmin
) {
    /**
     * Constructor compacto: validaciones mínimas de no-nulidad.
     * Las reglas de negocio más complejas se validan en el servicio.
     */
    public RegistrarTiendaCommand {
        if (nombreTienda == null || nombreTienda.isBlank()) {
            throw new IllegalArgumentException("El nombre de la tienda es obligatorio");
        }
        if (emailAdmin == null || emailAdmin.isBlank()) {
            throw new IllegalArgumentException("El email del administrador es obligatorio");
        }
        if (passwordAdmin == null || passwordAdmin.isBlank()) {
            throw new IllegalArgumentException("El password del administrador es obligatorio");
        }
        if (nombreAdmin == null || nombreAdmin.isBlank()) {
            throw new IllegalArgumentException("El nombre del administrador es obligatorio");
        }
        // Normalización
        nombreTienda = nombreTienda.strip();
        emailAdmin = emailAdmin.strip().toLowerCase();
        nombreAdmin = nombreAdmin.strip();
    }
}