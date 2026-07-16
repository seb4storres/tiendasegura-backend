package co.tiendasegura.ventas.infrastructure.config;

import co.tiendasegura.ventas.application.ports.in.RegistrarVentaUseCase;
import co.tiendasegura.ventas.application.services.RegistrarVentaService;
import co.tiendasegura.ventas.domain.ports.out.FiadosPort;
import co.tiendasegura.ventas.domain.ports.out.InventarioPort;
import co.tiendasegura.ventas.domain.ports.out.VentaRepositoryPort;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

/**
 * Factory de Micronaut que ensambla los casos de uso del dominio de ventas.
 *
 * @Transactional aquí es lo que hace ACID el "motor transaccional": la
 * creación de la venta, sus detalles, el descuento de stock (vía
 * InventarioPort) y el registro de deuda cuando aplica (vía FiadosPort)
 * ocurren en la misma conexión/transacción — los tres puertos comparten el
 * mismo DataSource, así que la propagación de transacción de Micronaut los
 * une en una sola unidad atómica. Si algo falla a mitad de camino, todo se
 * revierte: no quedan ventas sin stock descontado, stock descontado sin
 * venta registrada, ni deudas registradas sin su venta.
 */
@Factory
public class VentasUseCaseFactory {

    @Singleton
    @Transactional
    public RegistrarVentaUseCase registrarVentaUseCase(VentaRepositoryPort ventaRepository,
                                                        InventarioPort inventarioPort,
                                                        FiadosPort fiadosPort) {
        return new RegistrarVentaService(ventaRepository, inventarioPort, fiadosPort);
    }
}
