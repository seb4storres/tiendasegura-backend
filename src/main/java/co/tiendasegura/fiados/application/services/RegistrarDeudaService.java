package co.tiendasegura.fiados.application.services;

import co.tiendasegura.fiados.application.dto.RegistrarDeudaCommand;
import co.tiendasegura.fiados.application.ports.in.RegistrarDeudaUseCase;
import co.tiendasegura.fiados.domain.exceptions.ClienteNoEncontradoException;
import co.tiendasegura.fiados.domain.model.Cliente;
import co.tiendasegura.fiados.domain.ports.out.ClienteRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caso de uso: registrar una deuda contra el saldo de un cliente.
 * Es el punto de entrada que Ventas invoca (vía FiadosPort) cuando una
 * venta se paga con FIADO. Si Cliente.registrarDeuda() rechaza la deuda
 * (bloqueado / límite excedido), la excepción se propaga tal cual —
 * quien orquesta la transacción más amplia (RegistrarVentaService) se
 * encarga de que eso revierta la venta completa.
 */
public class RegistrarDeudaService implements RegistrarDeudaUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarDeudaService.class);

    private final ClienteRepositoryPort clienteRepository;

    public RegistrarDeudaService(ClienteRepositoryPort clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public void ejecutar(RegistrarDeudaCommand command) {
        Cliente cliente = clienteRepository.buscarPorId(command.tiendaId(), command.clienteId())
                .orElseThrow(() -> new ClienteNoEncontradoException(command.clienteId()));

        cliente.registrarDeuda(command.monto());
        clienteRepository.guardar(cliente);

        log.info("Deuda registrada: cliente {} +{} (venta {})",
                command.clienteId(), command.monto(), command.ventaId());
    }
}
