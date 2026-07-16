package co.tiendasegura.ventas.domain.ports.out;

import co.tiendasegura.ventas.domain.model.Venta;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia de Ventas.
 * guardar() persiste la venta y todos sus detalles como una sola operación;
 * la atomicidad real la garantiza el @Transactional del caso de uso.
 */
public interface VentaRepositoryPort {

    Venta guardar(Venta venta);

    Optional<Venta> buscarPorId(UUID tiendaId, UUID id);
}
