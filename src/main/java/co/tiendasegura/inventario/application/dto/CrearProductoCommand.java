package co.tiendasegura.inventario.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command inmutable para crear un producto nuevo.
 * El tiendaId NO viene del cliente HTTP — lo resuelve el controller
 * a partir del contexto de tenant (JWT), nunca del body de la petición.
 */
public record CrearProductoCommand(
        UUID tiendaId,
        String nombre,
        String codigoBarras,
        String descripcion,
        UUID categoriaId,
        BigDecimal precioCompra,
        BigDecimal precioVenta,
        int stockInicial,
        int stockMinimo
) {
    public CrearProductoCommand {
        if (tiendaId == null) {
            throw new IllegalArgumentException("El tiendaId es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (precioVenta == null || precioVenta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor a cero");
        }
        nombre = nombre.strip();
        if (codigoBarras != null) {
            codigoBarras = codigoBarras.isBlank() ? null : codigoBarras.strip();
        }
    }
}
