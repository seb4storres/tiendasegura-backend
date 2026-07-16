package co.tiendasegura.fiados.application.mapper;

import co.tiendasegura.fiados.application.dto.AbonoResponse;
import co.tiendasegura.fiados.application.dto.ClienteResponse;
import co.tiendasegura.fiados.domain.model.Abono;
import co.tiendasegura.fiados.domain.model.Cliente;

import java.math.BigDecimal;

/**
 * Mapper manual (sin MapStruct ni ModelMapper) para transformaciones
 * entre entidades de dominio y DTOs de respuesta.
 * Clase utilitaria con métodos estáticos — no necesita estado.
 */
public final class ClienteMapper {

    private ClienteMapper() {
        // No instanciable
    }

    public static ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getTiendaId(),
                cliente.getNombre(),
                cliente.getTelefono(),
                cliente.getCedula(),
                cliente.getDireccion(),
                cliente.getLimiteCredito(),
                cliente.getSaldoActual(),
                cliente.getEstado().name(),
                cliente.getVersion()
        );
    }

    public static AbonoResponse toAbonoResponse(Abono abono, BigDecimal saldoRestante) {
        return new AbonoResponse(
                abono.getId(),
                abono.getClienteId(),
                abono.getMonto(),
                abono.getMetodoPago(),
                abono.getNota(),
                abono.getFecha(),
                saldoRestante
        );
    }
}
