package co.tiendasegura.fiados.domain.ports.out;

import co.tiendasegura.fiados.domain.model.Abono;

/**
 * Puerto de salida para persistencia de Abonos.
 * Los abonos son inmutables una vez creados — no hay actualización.
 */
public interface AbonoRepositoryPort {

    Abono guardar(Abono abono);
}
