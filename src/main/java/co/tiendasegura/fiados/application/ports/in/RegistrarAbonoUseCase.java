package co.tiendasegura.fiados.application.ports.in;

import co.tiendasegura.fiados.application.dto.AbonoResponse;
import co.tiendasegura.fiados.application.dto.RegistrarAbonoCommand;

public interface RegistrarAbonoUseCase {

    AbonoResponse ejecutar(RegistrarAbonoCommand command);
}
