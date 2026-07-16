-- La tabla `productos` ya fue creada en V1__init_schema.sql.
-- Esta migración solo agrega las restricciones e índices que el
-- dominio de inventario necesita para operar de forma segura y eficiente.

-- Un código de barras no puede repetirse dentro de la misma tienda.
-- Parcial (WHERE codigo_barras IS NOT NULL) porque la columna es opcional
-- y varios productos sin código de barras no deben chocar entre sí.
CREATE UNIQUE INDEX uq_productos_tienda_codigo_barras
    ON productos (tienda_id, codigo_barras)
    WHERE codigo_barras IS NOT NULL;

-- Búsqueda de productos por nombre (autocompletado / búsqueda parcial en el POS).
CREATE INDEX idx_productos_nombre
    ON productos (tienda_id, nombre);
