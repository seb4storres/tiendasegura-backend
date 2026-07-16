package co.tiendasegura.auth.domain.ports.out;

import co.tiendasegura.auth.domain.model.Tienda;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia de Tiendas.
 * La implementación concreta (JDBC, JPA, etc.) vive en infraestructura.
 */
public interface TiendaRepositoryPort {

    Tienda guardar(Tienda tienda);

    Optional<Tienda> buscarPorId(UUID id);

    boolean existePorNit(String nit);
}