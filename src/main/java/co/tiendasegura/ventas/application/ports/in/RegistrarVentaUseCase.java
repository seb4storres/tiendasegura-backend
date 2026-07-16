package co.tiendasegura.ventas.application.ports.in;

import co.tiendasegura.ventas.application.dto.RegistrarVentaCommand;
import co.tiendasegura.ventas.application.dto.VentaResponse;

/**
 * Puerto de entrada: registrar una venta.
 * Idempotente por command.id() — reenviar el mismo comando no duplica
 * la venta ni vuelve a descontar stock.
 */
public interface RegistrarVentaUseCase {

    VentaResponse ejecutar(RegistrarVentaCommand command);
}
