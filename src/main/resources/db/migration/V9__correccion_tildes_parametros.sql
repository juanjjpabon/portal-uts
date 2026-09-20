-- Corrige tildes y enies faltantes en el texto sembrado por V4__parametros.sql.
-- Solo actualiza filas cuyo valor/descripcion siguen exactamente como se sembraron;
-- si el administrador ya edito el texto desde /admin/parametros, esa fila no se toca.

UPDATE parametro SET
    valor = 'La autoorientación es una herramienta de reflexión personal y anónima. No guarda tus respuestas, no genera un diagnóstico y no reemplaza la atención de un profesional. Al continuar confirmas que has leído este aviso.',
    descripcion = 'Aviso previo a la autoorientación (HU-08)'
WHERE clave = 'aviso.autoorientacion.previo'
  AND valor = 'La autoorientacion es una herramienta de reflexion personal y anonima. No guarda tus respuestas, no genera un diagnostico y no reemplaza la atencion de un profesional. Al continuar confirmas que has leido este aviso.';

UPDATE parametro SET
    valor = 'Este resultado es orientativo. No constituye un diagnóstico ni una valoración clínica. Si sientes que necesitas apoyo, consulta las rutas y contactos institucionales.',
    descripcion = 'Aviso de "no es un diagnóstico" en el resultado (HU-11)'
WHERE clave = 'aviso.autoorientacion.no_diagnostico'
  AND valor = 'Este resultado es orientativo. No constituye un diagnostico ni una valoracion clinica. Si sientes que necesitas apoyo, consulta las rutas y contactos institucionales.';

UPDATE parametro SET
    valor = 'Si tú o alguien más está en peligro inmediato, comunícate ya con la línea de emergencias 123 o acude al servicio de urgencias más cercano.',
    descripcion = 'Mensaje visible para situaciones urgentes (HU-06)'
WHERE clave = 'urgencia.mensaje'
  AND valor = 'Si tu o alguien mas esta en peligro inmediato, comunicate ya con la linea de emergencias 123 o acude al servicio de urgencias mas cercano.';

UPDATE parametro SET
    valor = 'Escribir al canal de orientación UTS',
    descripcion = 'Rótulo del botón del canal institucional (HU-07)'
WHERE clave = 'canal_institucional.etiqueta'
  AND valor = 'Escribir al canal de orientacion UTS';

UPDATE parametro SET
    descripcion = 'Destino del botón del canal institucional (HU-07)'
WHERE clave = 'canal_institucional.url'
  AND descripcion = 'Destino del boton del canal institucional (HU-07)';
