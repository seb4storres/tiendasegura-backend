package co.tiendasegura.inventario.application.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Respuesta HTTP con los datos de un producto.
 */
@Serdeable
public record ProductoResponse(
        UUID id,
        UUID tiendaId,
        String codigoBarras,
        String nombre,
        String descripcion,
        UUID categoriaId,
        BigDecimal precioCompra,
        BigDecimal precioVenta,
        int stock,
        int stockMinimo,
        boolean activo,
        int version
) {}
