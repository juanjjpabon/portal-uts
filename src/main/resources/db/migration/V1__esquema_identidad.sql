-- M05 Identidad y acceso: HU-16, HU-17, HU-21
-- Esquema base de usuarios y roles administrativos.

CREATE TABLE rol (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre      VARCHAR(40)  NOT NULL UNIQUE,
    descripcion VARCHAR(200) NOT NULL
);

CREATE TABLE usuario (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_completo  VARCHAR(120) NOT NULL,
    correo           VARCHAR(160) NOT NULL UNIQUE,
    hash_contrasena  VARCHAR(100) NOT NULL,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    ultimo_acceso    TIMESTAMPTZ,
    creado_en        TIMESTAMPTZ  NOT NULL,
    creado_por       VARCHAR(255),
    actualizado_en   TIMESTAMPTZ,
    actualizado_por  VARCHAR(255)
);

CREATE INDEX ix_usuario_correo ON usuario (LOWER(correo));

CREATE TABLE usuario_rol (
    usuario_id BIGINT NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    rol_id     BIGINT NOT NULL REFERENCES rol (id),
    PRIMARY KEY (usuario_id, rol_id)
);
