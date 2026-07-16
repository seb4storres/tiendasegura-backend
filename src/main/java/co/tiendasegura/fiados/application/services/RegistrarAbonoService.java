package co.tiendasegura.fiados.application.services;

import co.tiendasegura.fiados.application.dto.AbonoResponse;
import co.tiendasegura.fiados.application.dto.RegistrarAbonoCommand;
import co.tiendasegura.fiados.application.mapper.ClienteMapper;
import co.tiendasegura.fiados.application.ports.in.RegistrarAbonoUseCase;
import co.tiendasegura.fiados.domain.exceptions.ClienteNoEncontradoException;
import co.tiendasegura.fiados.domain.model.Abono;
import co.tiendasegura.fiados.domain.model.Cliente;
import co.tiendasegura.fiados.domain.ports.out.AbonoRepositoryPort;
import co.tiendasegura.fiados.domain.ports.out.ClienteRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caso de uso: registrar un abono (pago parcial o total) que reduce el
 * saldo de un cliente.
 */
public class RegistrarAbonoService implements RegistrarAbonoUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarAbonoService.class);

    private final ClienteRepositoryPort clienteRepository;
    private final AbonoRepositoryPort abonoRepository;

    public RegistrarAbonoService(ClienteRepositoryPort clienteRepository, AbonoRepositoryPort abonoRepository) {
        this.clienteRepository = clienteRepository;
        this.abonoRepository = abonoRepository;
    }

    @Override
    public AbonoResponse ejecutar(RegistrarAbonoCommand command) {
        Cliente cliente = clienteRepository.buscarPorId(command.tiendaId(), command.clienteId())
                .orElseThrow(() -> new ClienteNoEncontradoException(command.clienteId()));

        cliente.registrarAbono(command.monto());
        cliente = clienteRepository.guardar(cliente);

        Abono abono = Abono.crear(
                command.tiendaId(), command.clienteId(), command.monto(),
                command.metodoPago(), command.nota()
        );
        abono = abonoRepository.guardar(abono);

        log.info("Abono registrado: cliente {} -{} (saldo restante: {})",
                command.clienteId(), command.monto(), cliente.getSaldoActual());

        return ClienteMapper.toAbonoResponse(abono, cliente.getSaldoActual());
    }
}
