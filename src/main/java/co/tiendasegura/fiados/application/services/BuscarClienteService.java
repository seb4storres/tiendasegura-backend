package co.tiendasegura.fiados.application.services;

import co.tiendasegura.fiados.application.dto.BuscarClienteCommand;
import co.tiendasegura.fiados.application.dto.ClienteResponse;
import co.tiendasegura.fiados.application.mapper.ClienteMapper;
import co.tiendasegura.fiados.application.ports.in.BuscarClienteUseCase;
import co.tiendasegura.fiados.domain.exceptions.ClienteNoEncontradoException;
import co.tiendasegura.fiados.domain.model.Cliente;
import co.tiendasegura.fiados.domain.ports.out.ClienteRepositoryPort;

public class BuscarClienteService implements BuscarClienteUseCase {

    private final ClienteRepositoryPort clienteRepository;

    public BuscarClienteService(ClienteRepositoryPort clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public ClienteResponse ejecutar(BuscarClienteCommand command) {
        Cliente cliente = clienteRepository.buscarPorId(command.tiendaId(), command.id())
                .orElseThrow(() -> new ClienteNoEncontradoException(command.id()));

        return ClienteMapper.toResponse(cliente);
    }
}
