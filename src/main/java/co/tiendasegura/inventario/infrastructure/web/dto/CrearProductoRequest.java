package co.tiendasegura.inventario.infrastructure.web.dto;

import co.tiendasegura.inventario.application.dto.CrearProductoCommand;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO HTTP para el endpoint POST /productos.
 * Notar que NO incluye tiendaId: se resuelve en el controller
 * a partir del tenant context (JWT), nunca del body del cliente.
 */
@Serdeable
public record CrearProductoRequest(
        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
        String nombre,

        @Size(max = 50, message = "El código de barras no puede superar 50 caracteres")
        String codigoBarras,

        String descripcion,

        UUID categoriaId,

        @DecimalMin(value = "0.0", message = "El precio de compra no puede ser negativo")
        BigDecimal precioCompra,

        @NotNull(message = "El precio de venta es obligatorio")
        @Positive(message = "El precio de venta debe ser mayor a cero")
        BigDecimal precioVenta,

        @Min(value = 0, message = "El stock inicial no puede ser negativo")
        Integer stockInicial,

        @Min(value = 0, message = "El stock mínimo no puede ser negativo")
        Integer stockMinimo
) {
    public CrearProductoCommand toCommand(UUID tiendaId) {
        return new CrearProductoCommand(
                tiendaId,
                nombre,
                codigoBarras,
                descripcion,
                categoriaId,
                precioCompra != null ? precioCompra : BigDecimal.ZERO,
                precioVenta,
                stockInicial != null ? stockInicial : 0,
                stockMinimo != null ? stockMinimo : 0
        );
    }
}
