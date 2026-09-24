-- Corrige las tildes de los dos textos que V11 escribio en ASCII por error: el aviso
-- de contenido demostrativo (valor y descripcion) y el aviso previo a la
-- autoorientacion. Mismo patron que V9: cada UPDATE solo aplica si la fila sigue
-- exactamente como la dejo V11; si el administrador ya edito el texto desde
-- /admin/parametros, esa fila no se toca. (V11 no se puede corregir en su lugar:
-- Flyway guarda el checksum de cada migracion ya aplicada.)

UPDATE parametro SET
    valor = 'Este portal es un desarrollo académico (proyecto de grado UTS) con contenidos de ejemplo para fines de demostración. Para orientación oficial, usa el canal institucional.'
WHERE clave = 'aviso.contenido_demostrativo'
  AND valor = 'Este portal es un desarrollo academico (proyecto de grado UTS) con contenidos de ejemplo para fines de demostracion. Para orientacion oficial, usa el canal institucional.';

UPDATE parametro SET
    descripcion = 'Aviso de contenido demostrativo en páginas públicas'
WHERE clave = 'aviso.contenido_demostrativo'
  AND descripcion = 'Aviso de contenido demostrativo en paginas publicas';

UPDATE parametro SET
    valor = 'La autoorientación es una herramienta de reflexión personal y anónima: no se guardan tus respuestas ni tu resultado individual, y nadie puede identificar qué contestaste. Del cuestionario solo se llevan conteos agregados y anónimos (por ejemplo, cuántas personas obtuvieron cada nivel), sin ningún dato personal asociado. Esto no genera un diagnóstico ni reemplaza la atención de un profesional. Al continuar confirmas que has leído este aviso.'
WHERE clave = 'aviso.autoorientacion.previo'
  AND valor = 'La autoorientacion es una herramienta de reflexion personal y anonima: no se guardan tus respuestas ni tu resultado individual, y nadie puede identificar que contestaste. Del cuestionario solo se llevan conteos agregados y anonimos (por ejemplo, cuantas personas obtuvieron cada nivel), sin ningun dato personal asociado. Esto no genera un diagnostico ni reemplaza la atencion de un profesional. Al continuar confirmas que has leido este aviso.';
