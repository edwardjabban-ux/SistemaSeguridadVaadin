
CREATE DATABASE seguridad;

CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100),
    correo VARCHAR(100),
    telefono VARCHAR(30),
    documento VARCHAR(50),
    clave VARCHAR(100)
);

CREATE TABLE servicio (
    id SERIAL PRIMARY KEY,
    tipo VARCHAR(100),
    precio NUMERIC(10,2)
);

INSERT INTO servicio(tipo, precio)
VALUES
('Escolta Personal', 200000),
('Vehiculo Blindado', 500000),
('Instalacion de Camaras', 300000);

CREATE TABLE solicitud_servicio (
    id SERIAL PRIMARY KEY,
    usuario_id BIGINT REFERENCES usuario(id),
    servicios VARCHAR(1000),
    personalizacion VARCHAR(1000),
    fecha_servicio DATE,
    hora_servicio TIME,
    costo_total NUMERIC(12,2),
    metodo_pago VARCHAR(100),
    estado_pago VARCHAR(100),
    comprobante_pago VARCHAR(100),
    escolta_asignado VARCHAR(100),
    vehiculo_asignado VARCHAR(100),
    monitoreo VARCHAR(255),
    emergencia_activa BOOLEAN DEFAULT FALSE,
    calificacion INTEGER,
    comentario VARCHAR(1000),
    estado VARCHAR(100),
    fecha_creacion TIMESTAMP
);
