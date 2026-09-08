-- M06 HU-27: parametros operativos (avisos, urgencia, canal institucional).
-- Catalogo fijo; el administrador funcional edita el valor desde /admin/parametros.

CREATE TABLE parametro (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    clave           VARCHAR(80)  NOT NULL UNIQUE,
    valor           TEXT,
    descripcion     VARCHAR(200) NOT NULL,
    tipo            VARCHAR(20)  NOT NULL DEFAULT 'TEXTO_CORTO',
    grupo           VARCHAR(60)  NOT NULL,
    orden           INTEGER      NOT NULL DEFAULT 0,
    creado_en       TIMESTAMPTZ  NOT NULL,
    creado_por      VARCHAR(255),
    actualizado_en  TIMESTAMPTZ,
    actualizado_por VARCHAR(255),
    CONSTRAINT ck_parametro_tipo CHECK (tipo IN ('TEXTO_CORTO', 'TEXTO_LARGO', 'URL'))
);

INSERT INTO parametro (clave, valor, descripcion, tipo, grupo, orden, creado_en, creado_por) VALUES
    ('aviso.autoorientacion.previo',
     'La autoorientacion es una herramienta de reflexion personal y anonima. No guarda tus respuestas, no genera un diagnostico y no reemplaza la atencion de un profesional. Al continuar confirmas que has leido este aviso.',
     'Aviso previo a la autoorientacion (HU-08)', 'TEXTO_LARGO', 'Autoorientacion', 1, now(), 'sistema'),

    ('aviso.autoorientacion.no_diagnostico',
     'Este resultado es orientativo. No constituye un diagnostico ni una valoracion clinica. Si sientes que necesitas apoyo, consulta las rutas y contactos institucionales.',
     'Aviso de "no es un diagnostico" en el resultado (HU-11)', 'TEXTO_LARGO', 'Autoorientacion', 2, now(), 'sistema'),

    ('urgencia.mensaje',
     'Si tu o alguien mas esta en peligro inmediato, comunicate ya con la linea de emergencias 123 o acude al servicio de urgencias mas cercano.',
     'Mensaje visible para situaciones urgentes (HU-06)', 'TEXTO_LARGO', 'Urgencia', 1, now(), 'sistema'),

    ('canal_institucional.etiqueta',
     'Escribir al canal de orientacion UTS',
     'Rotulo del boton del canal institucional (HU-07)', 'TEXTO_CORTO', 'Canal institucional', 1, now(), 'sistema'),

    ('canal_institucional.url',
     'https://www.uts.edu.co/sitio/bienestar-institucional/',
     'Destino del boton del canal institucional (HU-07)', 'URL', 'Canal institucional', 2, now(), 'sistema');
