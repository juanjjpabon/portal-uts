-- M08 HU-22 (base) y M06 HU-21: bitacora de cambios administrativos + cambio
-- obligatorio de la contrasena inicial.

CREATE TABLE registro_bitacora (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ocurrido_en TIMESTAMPTZ  NOT NULL,
    actor       VARCHAR(160) NOT NULL,
    accion      VARCHAR(40)  NOT NULL,
    tipo_objeto VARCHAR(40)  NOT NULL,
    objeto_id   VARCHAR(40),
    descripcion VARCHAR(500) NOT NULL
);

CREATE INDEX ix_bitacora_objeto ON registro_bitacora (tipo_objeto, objeto_id);
CREATE INDEX ix_bitacora_fecha  ON registro_bitacora (ocurrido_en DESC);

-- HU-21: al crear o restablecer una contrasena se marca; el usuario debe cambiarla
-- en el primer ingreso. Los usuarios ya existentes no quedan forzados.
ALTER TABLE usuario ADD COLUMN debe_cambiar_clave BOOLEAN NOT NULL DEFAULT FALSE;
