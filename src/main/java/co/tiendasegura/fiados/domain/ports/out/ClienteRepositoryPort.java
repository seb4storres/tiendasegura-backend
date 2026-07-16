package co.tiendasegura.fiados.domain.ports.out;

import co.tiendasegura.fiados.domain.model.Cliente;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia de Clientes.
 * guardar() sirve tanto para creación como para actualización; la
 * actualización está protegida por optimistic concurrency control
 * (version) — ver ClienteJdbcAdapter.
 */
public interface ClienteRepositoryPort {

    Cliente guardar(Cliente cliente);

    Optional<Cliente> buscarPorId(UUID tiendaId, UUID id);
}
