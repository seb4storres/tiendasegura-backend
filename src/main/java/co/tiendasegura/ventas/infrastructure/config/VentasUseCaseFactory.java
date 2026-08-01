package co.tiendasegura.ventas.infrastructure.config;

import co.tiendasegura.ventas.application.ports.in.RegistrarVentaUseCase;
import co.tiendasegura.ventas.application.services.RegistrarVentaService;
import co.tiendasegura.ventas.domain.ports.out.FiadosPort;
import co.tiendasegura.ventas.domain.ports.out.InventarioPort;
import co.tiendasegura.ventas.domain.ports.out.VentaRepositoryPort;
import io.micronaut.context.annotation.Factory;
import io.micronaut.transaction.TransactionOperations;
import jakarta.inject.Singleton;

import java.sql.Connection;

/**
 * Factory de Micronaut que ensambla los casos de uso del dominio de ventas.
 *
 * A diferencia de otros casos de uso del proyecto, aquí NO se usa
 * @Transactional declarativo envolviendo todo el método: RegistrarVentaService
 * necesita demarcar su transacción de forma explícita (vía
 * TransactionOperations) para poder recuperarse de una carrera de
 * idempotencia (VentaDuplicadaException) SIN que esa recuperación quede
 * atrapada dentro de la misma transacción que hay que revertir. Ver el
 * Javadoc de RegistrarVentaService para el detalle completo.
 *
 * TransactionOperations<Connection> es el mismo mecanismo transaccional que
 * respalda a @Transactional en el resto del proyecto (micronaut-data-tx-jdbc
 * sobre el único DataSource "default") — sigue siendo ACID: creación de la
 * venta, sus detalles, el descuento de stock (vía InventarioPort) y el
 * registro de deuda cuando aplica (vía FiadosPort) ocurren en la misma
 * conexión/transacción.
 */
@Factory
public class VentasUseCaseFactory {

    @Singleton
    public RegistrarVentaUseCase registrarVentaUseCase(VentaRepositoryPort ventaRepository,
                                                        InventarioPort inventarioPort,
                                                        FiadosPort fiadosPort,
                                                        TransactionOperations<Connection> transactionOperations) {
        return new RegistrarVentaService(ventaRepository, inventarioPort, fiadosPort, transactionOperations);
    }
}
