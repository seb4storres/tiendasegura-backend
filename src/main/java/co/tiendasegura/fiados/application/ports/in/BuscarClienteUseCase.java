package co.tiendasegura.fiados.application.ports.in;

import co.tiendasegura.fiados.application.dto.BuscarClienteCommand;
import co.tiendasegura.fiados.application.dto.ClienteResponse;

public interface BuscarClienteUseCase {

    ClienteResponse ejecutar(BuscarClienteCommand command);
}
