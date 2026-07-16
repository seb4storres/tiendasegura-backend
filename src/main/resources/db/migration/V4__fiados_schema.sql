-- Simplificamos el modelo de crédito a un saldo corrido por cliente
-- (en vez de un registro de deuda por venta individual).
--
-- `abonos` pasa a aplicar directamente sobre el saldo del cliente, no
-- sobre un fiado específico. Hay que soltar la columna (y con ella su FK
-- hacia `fiados`) ANTES de poder eliminar la tabla `fiados`.
ALTER TABLE abonos DROP COLUMN fiado_id;
ALTER TABLE abonos ADD COLUMN cliente_id UUID NOT NULL REFERENCES clientes(id);

-- La tabla `fiados` de V1 queda sin uso bajo este diseño — se elimina
-- porque está vacía (el dominio de fiados nunca se implementó hasta
-- ahora, no hay datos que perder).
DROP TABLE fiados;

CREATE INDEX idx_abonos_cliente_id
    ON abonos (cliente_id);

-- `clientes.activo` (boolean genérico) se reemplaza por `estado`, que
-- distingue explícitamente un cliente BLOQUEADO (no puede adquirir más
-- crédito) de uno simplemente activo — la misma convención que ya usa
-- `ventas.estado`.
ALTER TABLE clientes DROP COLUMN activo;
ALTER TABLE clientes ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';
ALTER TABLE clientes ADD CONSTRAINT chk_clientes_estado CHECK (estado IN ('ACTIVO', 'BLOQUEADO'));
