-- M06 Administracion de contenido: HU-18 (contenidos/categorias) y HU-19 (rutas/contactos).
-- Modelo generico F-DC-125: una tabla "recurso" con columna "tipo".

CREATE TABLE categoria (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre          VARCHAR(80)  NOT NULL UNIQUE,
    slug            VARCHAR(90)  NOT NULL UNIQUE,
    descripcion     VARCHAR(300),
    activa          BOOLEAN      NOT NULL DEFAULT TRUE,
    orden           INTEGER      NOT NULL DEFAULT 0,
    creado_en       TIMESTAMPTZ  NOT NULL,
    creado_por      VARCHAR(255),
    actualizado_en  TIMESTAMPTZ,
    actualizado_por VARCHAR(255)
);

CREATE TABLE recurso (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo            VARCHAR(20)  NOT NULL,        -- CONTENIDO | RUTA | CONTACTO
    titulo          VARCHAR(160) NOT NULL,
    slug            VARCHAR(180) UNIQUE,          -- solo CONTENIDO (HU-01)
    -- CONTENIDO (HU-04)
    resumen         VARCHAR(500),
    cuerpo          TEXT,
    fuente          VARCHAR(300),
    -- RUTA / CONTACTO (HU-05)
    dependencia     VARCHAR(160),
    horario         VARCHAR(200),
    canal           VARCHAR(200),
    url_canal       VARCHAR(300),
    urgente         BOOLEAN      NOT NULL DEFAULT FALSE,
    -- comunes
    estado          VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR',
    orden           INTEGER      NOT NULL DEFAULT 0,
    publicado_en    TIMESTAMPTZ,
    creado_en       TIMESTAMPTZ  NOT NULL,
    creado_por      VARCHAR(255),
    actualizado_en  TIMESTAMPTZ,
    actualizado_por VARCHAR(255),
    CONSTRAINT ck_recurso_tipo   CHECK (tipo IN ('CONTENIDO', 'RUTA', 'CONTACTO')),
    CONSTRAINT ck_recurso_estado CHECK (estado IN ('BORRADOR', 'PUBLICADO', 'ARCHIVADO'))
);

CREATE INDEX ix_recurso_tipo_estado ON recurso (tipo, estado);
CREATE INDEX ix_recurso_tipo_urgente ON recurso (tipo, urgente);

CREATE TABLE recurso_categoria (
    recurso_id   BIGINT NOT NULL REFERENCES recurso (id) ON DELETE CASCADE,
    categoria_id BIGINT NOT NULL REFERENCES categoria (id) ON DELETE CASCADE,
    PRIMARY KEY (recurso_id, categoria_id)
);
