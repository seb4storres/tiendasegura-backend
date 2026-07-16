-- Las tablas `ventas` y `detalle_ventas` ya existen desde V1__init_schema.sql,
-- incluyendo `ventas.id` como PRIMARY KEY SIN DEFAULT — eso es intencional:
-- el ID lo genera el cliente y actúa como clave de idempotencia (un INSERT
-- con un id repetido choca contra la PK, señal que el caso de uso usa para
-- detectar reintentos duplicados en redes intermitentes).
--
-- Esta migración solo agrega los índices que el dominio de ventas necesita
-- para operar de forma eficiente y aislada por tienda.

-- Detalles de una venta específica (usado en cada lectura idempotente y en
-- cualquier consulta futura de recibo/factura).
CREATE INDEX idx_detalle_ventas_venta_id
    ON detalle_ventas (venta_id);

-- Listar ventas de una tienda ordenadas por fecha (reportes, consulta de caja).
CREATE INDEX idx_ventas_tienda_fecha
    ON ventas (tienda_id, fecha DESC);
