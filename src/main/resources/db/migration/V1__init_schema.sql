CREATE TABLE tiendas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(150) NOT NULL,
    nit VARCHAR(20),
    direccion VARCHAR(255),
    telefono VARCHAR(20),
    plan VARCHAR(30) NOT NULL DEFAULT 'BASICO',
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tienda_id UUID NOT NULL REFERENCES tiendas(id),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'CAJERO',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (email)
);

CREATE TABLE categorias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tienda_id UUID NOT NULL REFERENCES tiendas(id),
    nombre VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tienda_id, nombre)
);

CREATE TABLE productos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tienda_id UUID NOT NULL REFERENCES tiendas(id),
    codigo_barras VARCHAR(50),
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    categoria_id UUID REFERENCES categorias(id),
    precio_compra NUMERIC(12,2) NOT NULL DEFAULT 0,
    precio_venta NUMERIC(12,2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    stock_minimo INTEGER NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE clientes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tienda_id UUID NOT NULL REFERENCES tiendas(id),
    nombre VARCHAR(200) NOT NULL,
    telefono VARCHAR(20),
    cedula VARCHAR(20),
    direccion VARCHAR(255),
    limite_credito NUMERIC(12,2) NOT NULL DEFAULT 0,
    saldo_pendiente NUMERIC(12,2) NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE ventas (
    id UUID PRIMARY KEY,
    tienda_id UUID NOT NULL REFERENCES tiendas(id),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    cliente_id UUID REFERENCES clientes(id),
    fecha TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    subtotal NUMERIC(12,2) NOT NULL,
    descuento NUMERIC(12,2) NOT NULL DEFAULT 0,
    total NUMERIC(12,2) NOT NULL,
    metodo_pago VARCHAR(20) NOT NULL,
    monto_recibido NUMERIC(12,2),
    cambio NUMERIC(12,2),
    estado VARCHAR(20) NOT NULL DEFAULT 'COMPLETADA',
    notas TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    synced_at TIMESTAMPTZ
);

CREATE TABLE detalle_ventas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    venta_id UUID NOT NULL REFERENCES ventas(id) ON DELETE CASCADE,
    tienda_id UUID NOT NULL REFERENCES tiendas(id),
    producto_id UUID NOT NULL REFERENCES productos(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario_snapshot NUMERIC(12,2) NOT NULL,
    nombre_producto_snapshot VARCHAR(200) NOT NULL,
    subtotal NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE fiados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tienda_id UUID NOT NULL REFERENCES tiendas(id),
    cliente_id UUID NOT NULL REFERENCES clientes(id),
    venta_id UUID NOT NULL REFERENCES ventas(id),
    monto_total NUMERIC(12,2) NOT NULL,
    saldo_pendiente NUMERIC(12,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE abonos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tienda_id UUID NOT NULL REFERENCES tiendas(id),
    fiado_id UUID NOT NULL REFERENCES fiados(id),
    monto NUMERIC(12,2) NOT NULL CHECK (monto > 0),
    metodo_pago VARCHAR(20) NOT NULL DEFAULT 'EFECTIVO',
    nota TEXT,
    fecha TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE idempotency_keys (
    key UUID PRIMARY KEY,
    tienda_id UUID NOT NULL REFERENCES tiendas(id),
    entity_type VARCHAR(30) NOT NULL,
    entity_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
