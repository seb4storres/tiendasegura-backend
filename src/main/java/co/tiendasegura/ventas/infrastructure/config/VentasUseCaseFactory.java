package co.tiendasegura.ventas.infrastructure.config;

import co.tiendasegura.ventas.application.ports.in.RegistrarVentaUseCase;
import co.tiendasegura.ventas.application.services.RegistrarVentaService;
import co.tiendasegura.ventas.domain.ports.out.InventarioPort;
import co.tiendasegura.ventas.domain.ports.out.VentaRepositoryPort;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

/**
 * Factory de Micronaut que ensambla los casos de uso del dominio de ventas.
 *
 * @Transactional aquí es lo que hace ACID el "motor transaccional": la
 * creación de la venta, sus detalles y el descuento de stock (a través de
 * InventarioPort, que internamente usa el mismo DataSource) ocurren en la
 * misma conexión/transacción. Si algo falla a mitad de camino, todo se
 * revierte — no quedan ventas sin stock descontado, ni stock descontado
 * sin venta registrada.
 */
@Factory
public class VentasUseCaseFactory {

    @Singleton
    @Transactional
    public RegistrarVentaUseCase registrarVentaUseCase(VentaRepositoryPort ventaRepository,
                                                        InventarioPort inventarioPort) {
        return new RegistrarVentaService(ventaRepository, inventarioPort);
    }
}
