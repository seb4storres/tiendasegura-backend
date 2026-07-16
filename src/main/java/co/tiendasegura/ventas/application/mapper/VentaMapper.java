package co.tiendasegura.ventas.application.mapper;

import co.tiendasegura.ventas.application.dto.VentaResponse;
import co.tiendasegura.ventas.domain.model.DetalleVenta;
import co.tiendasegura.ventas.domain.model.Venta;

/**
 * Mapper manual (sin MapStruct ni ModelMapper) para transformaciones
 * entre entidades de dominio y DTOs de respuesta.
 * Clase utilitaria con métodos estáticos — no necesita estado.
 */
public final class VentaMapper {

    private VentaMapper() {
        // No instanciable
    }

    public static VentaResponse toResponse(Venta venta) {
        return new VentaResponse(
                venta.getId(),
                venta.getTiendaId(),
                venta.getCajeroId(),
                venta.getClienteId(),
                venta.getFecha(),
                venta.getSubtotal(),
                venta.getDescuento(),
                venta.getTotal(),
                venta.getMetodoPago().name(),
                venta.getMontoRecibido(),
                venta.getCambio(),
                venta.getEstado().name(),
                venta.getDetalles().stream().map(VentaMapper::toDetalleResponse).toList()
        );
    }

    private static VentaResponse.DetalleVentaResponse toDetalleResponse(DetalleVenta detalle) {
        return new VentaResponse.DetalleVentaResponse(
                detalle.getId(),
                detalle.getProductoId(),
                detalle.getNombreProductoSnapshot(),
                detalle.getCantidad(),
                detalle.getPrecioUnitarioSnapshot(),
                detalle.getSubtotal()
        );
    }
}
