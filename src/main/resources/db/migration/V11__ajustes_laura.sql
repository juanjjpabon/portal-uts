-- Ajustes pedidos por la directora (correo del 23/9/2026), alcance reducido sin rol
-- nuevo -- ver claude/plan_cambios_fdc125.md #4.

-- #1: aviso configurable de contenido demostrativo, visible en toda pagina publica
-- (ParametroService.valor lo consume desde fragments/comunes.html :: navbarPublico).
-- Vacio = no se muestra nada; el admin funcional lo edita/borra desde /admin/parametros.
INSERT INTO parametro (clave, valor, descripcion, tipo, grupo, orden, creado_en, creado_por) VALUES
    ('aviso.contenido_demostrativo',
     'Este portal es un desarrollo academico (proyecto de grado UTS) con contenidos de ejemplo para fines de demostracion. Para orientacion oficial, usa el canal institucional.',
     'Aviso de contenido demostrativo en paginas publicas', 'TEXTO_LARGO', 'General', 1, now(), 'sistema');

-- #7: mensaje de privacidad corregido -- deja explicito que no hay dato individual
-- (ni respuesta ni resultado) pero si conteos agregados anonimos (HU-13/HU-23). Solo
-- actualiza la fila si nunca se ha tocado desde /admin/parametros (actualizado_en
-- sigue en null, tal como la dejo el INSERT de V4); si el admin ya la personalizo en
-- produccion, esta migracion no la pisa.
--
-- Se guarda por actualizado_en IS NULL y no comparando el valor con el texto exacto
-- que sembro V4: un intento anterior de esta misma migracion comparaba contra una
-- transcripcion manual de ese texto que perdio las tildes por error, no coincidia
-- nunca con el valor real en la base de datos y por lo tanto nunca actualizaba nada.
UPDATE parametro
SET valor = 'La autoorientacion es una herramienta de reflexion personal y anonima: no se guardan tus respuestas ni tu resultado individual, y nadie puede identificar que contestaste. Del cuestionario solo se llevan conteos agregados y anonimos (por ejemplo, cuantas personas obtuvieron cada nivel), sin ningun dato personal asociado. Esto no genera un diagnostico ni reemplaza la atencion de un profesional. Al continuar confirmas que has leido este aviso.',
    actualizado_en = now(),
    actualizado_por = 'sistema'
WHERE clave = 'aviso.autoorientacion.previo'
  AND actualizado_en IS NULL;
