package co.tiendasegura.ventas.domain.ports.out;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Puerto de salida hacia el dominio de Fiados.
 *
 * Igual que InventarioPort: Ventas NO conoce las clases de dominio de
 * Fiados. Solo pide "registra esta deuda" — es Fiados quien decide si el
 * cliente puede o no adquirir ese crédito, y lo comunica lanzando una
 * excepción (ver FiadosPortAdapter) que revierte toda la transacción de
 * la venta si la deuda no se puede registrar.
 */
public interface FiadosPort {

    void registrarDeuda(UUID tiendaId, UUID clienteId, UUID ventaId, BigDecimal monto);
}
