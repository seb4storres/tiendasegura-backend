package co.tiendasegura.fiados.application.ports.in;

import co.tiendasegura.fiados.application.dto.ClienteResponse;
import co.tiendasegura.fiados.application.dto.CrearClienteCommand;

public interface CrearClienteUseCase {

    ClienteResponse ejecutar(CrearClienteCommand command);
}
