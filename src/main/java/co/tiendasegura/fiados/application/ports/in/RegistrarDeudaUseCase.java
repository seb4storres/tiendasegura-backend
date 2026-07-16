package co.tiendasegura.fiados.application.ports.in;

import co.tiendasegura.fiados.application.dto.RegistrarDeudaCommand;

/**
 * Puerto de entrada usado por otros dominios (Ventas) para registrar una
 * deuda contra un cliente. Lanza excepciones de dominio (cliente no
 * encontrado, bloqueado, o límite de crédito excedido) que el caller debe
 * dejar propagar — normalmente eso implica revertir toda la operación que
 * originó la deuda.
 */
public interface RegistrarDeudaUseCase {

    void ejecutar(RegistrarDeudaCommand command);
}
