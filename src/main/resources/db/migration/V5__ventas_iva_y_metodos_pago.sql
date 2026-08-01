-- `metodo_pago` y `subtotal` YA existen en `ventas` desde V1__init_schema.sql.
-- Lo único nuevo es `iva`: los precios de producto ya incluyen IVA (19%),
-- así que se extrae del `total` ya calculado (no se le suma al cliente).

ALTER TABLE ventas ADD COLUMN iva NUMERIC(12,2);

UPDATE ventas
SET iva = ROUND(total - (total / 1.19), 2)
WHERE iva IS NULL;

ALTER TABLE ventas ALTER COLUMN iva SET NOT NULL;
ALTER TABLE ventas ADD CONSTRAINT chk_ventas_iva_no_negativo CHECK (iva >= 0);

-- El enum de dominio MetodoPago reemplaza TRANSFERENCIA (genérico) por los
-- proveedores concretos BANCOLOMBIA y DAVIPLATA. Ventas históricas con
-- TRANSFERENCIA se migran a BANCOLOMBIA por ser el proveedor más común;
-- ajustar manualmente si se conoce el proveedor real de esas ventas.
UPDATE ventas SET metodo_pago = 'BANCOLOMBIA' WHERE metodo_pago = 'TRANSFERENCIA';
