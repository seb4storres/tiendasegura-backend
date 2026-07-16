package co.tiendasegura.auth.domain.ports.out;

import co.tiendasegura.auth.domain.model.Usuario;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia de Usuarios.
 * Notar que buscarPorEmail NO filtra por tienda_id:
 * el email es único global (un email = una sola cuenta en todo el SaaS).
 */
public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorId(UUID id);

    boolean existeEmail(String email);
}