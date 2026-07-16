package co.tiendasegura.fiados.infrastructure.config;

import co.tiendasegura.fiados.application.ports.in.BuscarClienteUseCase;
import co.tiendasegura.fiados.application.ports.in.CrearClienteUseCase;
import co.tiendasegura.fiados.application.ports.in.RegistrarAbonoUseCase;
import co.tiendasegura.fiados.application.ports.in.RegistrarDeudaUseCase;
import co.tiendasegura.fiados.application.services.BuscarClienteService;
import co.tiendasegura.fiados.application.services.CrearClienteService;
import co.tiendasegura.fiados.application.services.RegistrarAbonoService;
import co.tiendasegura.fiados.application.services.RegistrarDeudaService;
import co.tiendasegura.fiados.domain.ports.out.AbonoRepositoryPort;
import co.tiendasegura.fiados.domain.ports.out.ClienteRepositoryPort;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

/**
 * Factory de Micronaut que ensambla los casos de uso del dominio de fiados.
 *
 * @Transactional en registrarDeudaUseCase importa especialmente: cuando
 * Ventas invoca este caso de uso desde dentro de su propia transacción
 * (al registrar una venta FIADO), Micronaut une esta ejecución a esa misma
 * transacción en curso (propagación REQUIRED) en vez de abrir una nueva —
 * si el límite de crédito se excede, la venta completa se revierte junto
 * con la deuda.
 */
@Factory
public class FiadosUseCaseFactory {

    @Singleton
    @Transactional
    public CrearClienteUseCase crearClienteUseCase(ClienteRepositoryPort clienteRepository) {
        return new CrearClienteService(clienteRepository);
    }

    @Singleton
    @Transactional
    public BuscarClienteUseCase buscarClienteUseCase(ClienteRepositoryPort clienteRepository) {
        return new BuscarClienteService(clienteRepository);
    }

    @Singleton
    @Transactional
    public RegistrarDeudaUseCase registrarDeudaUseCase(ClienteRepositoryPort clienteRepository) {
        return new RegistrarDeudaService(clienteRepository);
    }

    @Singleton
    @Transactional
    public RegistrarAbonoUseCase registrarAbonoUseCase(ClienteRepositoryPort clienteRepository,
                                                        AbonoRepositoryPort abonoRepository) {
        return new RegistrarAbonoService(clienteRepository, abonoRepository);
    }
}
