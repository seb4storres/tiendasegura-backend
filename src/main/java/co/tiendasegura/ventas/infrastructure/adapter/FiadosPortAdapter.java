package co.tiendasegura.ventas.infrastructure.adapter;

import co.tiendasegura.fiados.application.dto.RegistrarDeudaCommand;
import co.tiendasegura.fiados.application.ports.in.RegistrarDeudaUseCase;
import co.tiendasegura.ventas.domain.ports.out.FiadosPort;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Adaptador que traduce entre el dominio de Ventas y el dominio de Fiados.
 * Es el ÚNICO punto del código donde `ventas` conoce tipos de `fiados` —
 * el mismo patrón que InventarioPortAdapter usa hacia Inventario.
 */
@Singleton
public class FiadosPortAdapter implements FiadosPort {

    private final RegistrarDeudaUseCase registrarDeudaUseCase;

    public FiadosPortAdapter(RegistrarDeudaUseCase registrarDeudaUseCase) {
        this.registrarDeudaUseCase = registrarDeudaUseCase;
    }

    @Override
    public void registrarDeuda(UUID tiendaId, UUID clienteId, UUID ventaId, BigDecimal monto) {
        registrarDeudaUseCase.ejecutar(new RegistrarDeudaCommand(tiendaId, clienteId, ventaId, monto));
    }
}
