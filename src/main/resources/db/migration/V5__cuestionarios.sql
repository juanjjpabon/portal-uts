-- M06 HU-20: cuestionarios de autoorientacion con control de versiones (F-DC-125).

CREATE TABLE cuestionario (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre          VARCHAR(120) NOT NULL,
    descripcion     VARCHAR(500),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ  NOT NULL,
    creado_por      VARCHAR(255),
    actualizado_en  TIMESTAMPTZ,
    actualizado_por VARCHAR(255)
);

CREATE TABLE cuestionario_version (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cuestionario_id BIGINT      NOT NULL REFERENCES cuestionario (id) ON DELETE CASCADE,
    numero          INTEGER     NOT NULL,
    estado          VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    publicada_en    TIMESTAMPTZ,
    notas_version   VARCHAR(300),
    creado_en       TIMESTAMPTZ NOT NULL,
    creado_por      VARCHAR(255),
    actualizado_en  TIMESTAMPTZ,
    actualizado_por VARCHAR(255),
    CONSTRAINT uq_version_numero UNIQUE (cuestionario_id, numero),
    CONSTRAINT ck_version_estado CHECK (estado IN ('BORRADOR', 'PUBLICADA', 'ARCHIVADA'))
);

-- A lo sumo una version PUBLICADA por cuestionario (HU-20).
CREATE UNIQUE INDEX uq_version_publicada
    ON cuestionario_version (cuestionario_id)
    WHERE estado = 'PUBLICADA';

CREATE TABLE pregunta (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id  BIGINT       NOT NULL REFERENCES cuestionario_version (id) ON DELETE CASCADE,
    enunciado   VARCHAR(300) NOT NULL,
    orden       INTEGER      NOT NULL DEFAULT 0,
    obligatoria BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE opcion (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pregunta_id BIGINT       NOT NULL REFERENCES pregunta (id) ON DELETE CASCADE,
    texto       VARCHAR(200) NOT NULL,
    valor       INTEGER      NOT NULL DEFAULT 0,
    orden       INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE nivel_resultado (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id  BIGINT        NOT NULL REFERENCES cuestionario_version (id) ON DELETE CASCADE,
    nombre      VARCHAR(80)   NOT NULL,
    orden       INTEGER       NOT NULL DEFAULT 0,
    puntaje_min INTEGER       NOT NULL DEFAULT 0,
    puntaje_max INTEGER       NOT NULL DEFAULT 0,
    explicacion VARCHAR(1000) NOT NULL DEFAULT '',
    color       VARCHAR(20)
);

CREATE TABLE recomendacion (
    id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nivel_id BIGINT       NOT NULL REFERENCES nivel_resultado (id) ON DELETE CASCADE,
    texto    VARCHAR(500) NOT NULL,
    orden    INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE nivel_ruta (
    nivel_id   BIGINT NOT NULL REFERENCES nivel_resultado (id) ON DELETE CASCADE,
    recurso_id BIGINT NOT NULL REFERENCES recurso (id) ON DELETE CASCADE,
    PRIMARY KEY (nivel_id, recurso_id)
);

CREATE INDEX ix_pregunta_version ON pregunta (version_id);
CREATE INDEX ix_nivel_version ON nivel_resultado (version_id);
