package co.tiendasegura.fiados.application.services;

import co.tiendasegura.fiados.application.dto.ClienteResponse;
import co.tiendasegura.fiados.application.dto.CrearClienteCommand;
import co.tiendasegura.fiados.application.mapper.ClienteMapper;
import co.tiendasegura.fiados.application.ports.in.CrearClienteUseCase;
import co.tiendasegura.fiados.domain.model.Cliente;
import co.tiendasegura.fiados.domain.ports.out.ClienteRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrearClienteService implements CrearClienteUseCase {

    private static final Logger log = LoggerFactory.getLogger(CrearClienteService.class);

    private final ClienteRepositoryPort clienteRepository;

    public CrearClienteService(ClienteRepositoryPort clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public ClienteResponse ejecutar(CrearClienteCommand command) {
        log.info("Creando cliente '{}' para tienda {}", command.nombre(), command.tiendaId());

        Cliente cliente = Cliente.crear(
                command.tiendaId(), command.nombre(), command.telefono(),
                command.cedula(), command.direccion(), command.limiteCredito()
        );
        cliente = clienteRepository.guardar(cliente);

        log.debug("Cliente creado con ID: {}", cliente.getId());

        return ClienteMapper.toResponse(cliente);
    }
}
