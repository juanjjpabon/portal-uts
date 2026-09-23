-- Reunion con la directora (22/9/2026): el portal se ve "plano" (solo texto) y las
-- rutas publicadas son en realidad contactos, no dicen que pasos seguir.
--
--  1) imagen: imagenes subidas desde el panel (folletos del CAE, banners, fotos de
--     los pasos de una ruta). Se guardan en la propia base de datos (bytea) y no en
--     disco, porque Render borra el disco del contenedor en cada despliegue y la
--     base de datos no. El id es un UUID: se usa en la URL publica (/imagenes/{id})
--     y asi no se pueden recorrer las imagenes adivinando numeros consecutivos.
--  2) recurso: imagen principal, texto alternativo (accesibilidad, lectores de
--     pantalla) y "destacado" (aparece en el carrusel de la portada).
--  3) paso_ruta: pasos ordenados de una ruta institucional ("Paso 1: ...").

CREATE TABLE imagen (
    id              UUID         PRIMARY KEY,
    tipo_contenido  VARCHAR(40)  NOT NULL,
    nombre_original VARCHAR(200),
    ancho           INTEGER      NOT NULL,
    alto            INTEGER      NOT NULL,
    tamano_bytes    INTEGER      NOT NULL,
    datos           BYTEA        NOT NULL,
    creado_en       TIMESTAMPTZ  NOT NULL,
    creado_por      VARCHAR(255),
    actualizado_en  TIMESTAMPTZ,
    actualizado_por VARCHAR(255),
    CONSTRAINT ck_imagen_tipo
        CHECK (tipo_contenido IN ('image/jpeg', 'image/png', 'image/gif', 'image/webp'))
);

ALTER TABLE recurso ADD COLUMN imagen_id UUID REFERENCES imagen (id) ON DELETE SET NULL;
ALTER TABLE recurso ADD COLUMN imagen_alt VARCHAR(250);
ALTER TABLE recurso ADD COLUMN destacado BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX ix_recurso_destacado ON recurso (destacado, estado);

CREATE TABLE paso_ruta (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    recurso_id      BIGINT       NOT NULL REFERENCES recurso (id) ON DELETE CASCADE,
    orden           INTEGER      NOT NULL,
    titulo          VARCHAR(160) NOT NULL,
    descripcion     TEXT,
    imagen_id       UUID         REFERENCES imagen (id) ON DELETE SET NULL,
    imagen_alt      VARCHAR(250),
    creado_en       TIMESTAMPTZ  NOT NULL,
    creado_por      VARCHAR(255),
    actualizado_en  TIMESTAMPTZ,
    actualizado_por VARCHAR(255)
);

CREATE INDEX ix_paso_ruta_recurso ON paso_ruta (recurso_id, orden);
