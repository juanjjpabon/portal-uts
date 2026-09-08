# Historias de Usuario y Product Backlog
Proyecto 65-2026-015 — Portal de orientación psicosocial UTS

**Alcance confirmado:** 27 de los 30 RF originales del Documento de Alcance Funcional. Quedan fuera de esta entrega: RF-14 y RF-15 (historial privado del estudiante) y, por dependencia de estos, RF-20 (consentimiento para funciones personales), CU-03 y RN-06. Ningún otro requisito "Debe" fue eliminado; RF-16, RF-17, RF-26, RF-27 y RF-28 se mantienen en versión simplificada, según lo autorizado por la directora del trabajo de grado.

## Historias de usuario

### M01 — Portal informativo

**HU-01 (RF-01).** Como visitante, quiero consultar contenidos preventivos sin iniciar sesión, para acceder a la información sin barreras.
*Criterio:* un contenido publicado se abre desde la página principal sin autenticarse.

**HU-02 (RF-02).** Como visitante, quiero explorar los contenidos por categorías temáticas, para encontrar información relacionada con mi necesidad.
*Criterio:* cada contenido tiene al menos una categoría y es accesible desde ella.

**HU-03 (RF-03).** Como visitante, quiero buscar contenidos, rutas y recursos por palabra clave, para encontrar información sin navegar por categorías.
*Criterio:* una búsqueda devuelve coincidencias pertinentes o informa que no hay resultados.

**HU-04 (RF-04).** Como visitante, quiero ver información de factores y señales de alerta en lenguaje claro y no estigmatizante, para reconocer situaciones relevantes sin sentirme juzgado.
*Criterio:* el contenido muestra título, resumen, cuerpo, fecha de actualización y fuente.

### M02 — Rutas y contactos

**HU-05 (RF-05).** Como visitante, quiero consultar las rutas institucionales con dependencia, horario y canal, para saber a quién acudir.
*Criterio:* la ruta muestra todos los campos obligatorios y fecha de última revisión.

**HU-06 (RF-06).** Como visitante, quiero ver información visible para situaciones urgentes, para encontrarla rápido si la necesito.
*Criterio:* desde cualquier resultado de autoorientación se llega a la información urgente en un clic.

**HU-07 (RF-07).** Como visitante, quiero abrir el canal institucional oficial desde un botón, para contactar sin que el portal gestione mi caso.
*Criterio:* el botón dirige al canal configurado; el portal no asigna responsable ni hace seguimiento.

### M03 — Autoorientación

**HU-08 (RF-08).** Como visitante, quiero leer un aviso antes de iniciar la autoorientación, para entender su propósito y límites.
*Criterio:* el formulario no inicia hasta confirmar que leí el aviso.

**HU-09 (RF-09).** Como visitante, quiero responder un formulario validado, para reflexionar sobre mi situación.
*Criterio:* puedo avanzar, retroceder y enviar solo si se cumplen las validaciones.

**HU-10 (RF-10).** Como visitante, quiero recibir un nivel orientativo calculado con reglas configurables, para tener una referencia.
*Criterio:* con respuestas de prueba conocidas, el sistema produce el nivel esperado.

**HU-11 (RF-11).** Como visitante, quiero un resumen comprensible del resultado con aviso de que no es diagnóstico, para interpretarlo correctamente.
*Criterio:* el resultado incluye nivel, explicación breve y aviso no diagnóstico.

**HU-12 (RF-12).** Como visitante, quiero recomendaciones y rutas según mi resultado, para saber qué hacer después.
*Criterio:* cada nivel devuelve al menos una recomendación y una ruta aplicable.

**HU-13 (RF-13).** Como visitante, quiero que mi autoorientación sea anónima y no se guarde, para sentirme seguro al usarla.
*Criterio:* al finalizar o abandonar, no queda registro individual recuperable.

### M04 — Orientación docente (reorganizado como categoría de contenido)

**HU-14 (RF-16).** Como docente, quiero consultar contenido categorizado para orientar inicialmente a un estudiante, para saber cómo actuar sin acceder a datos de nadie.
*Criterio:* el docente consulta señales generales y recomendaciones desde las categorías, sin ver resultados individuales.

**HU-15 (RF-17).** Como docente, quiero que el contenido explique los límites de mi rol y cuándo remitir, para actuar dentro de lo que me corresponde.
*Criterio:* el contenido diferencia escuchar/orientar/remitir de diagnosticar/intervenir/prometer confidencialidad absoluta.

### M05 — Identidad y acceso

**HU-16 (RF-18).** Como administrador, quiero iniciar y cerrar sesión para acceder a funciones protegidas, para que solo personal autorizado las use.
*Criterio:* credenciales válidas habilitan el rol; al cerrar sesión se invalida el acceso.

**HU-17 (RF-19).** Como administrador, quiero que mis permisos se apliquen según mi rol, para no ejecutar operaciones que no me corresponden.
*Criterio:* una persona sin permiso recibe denegación al intentar una operación administrativa.

### M06 — Administración

**HU-18 (RF-21).** Como administrador, quiero crear, editar y eliminar contenidos y categorías desde el aplicativo, para mantenerlos actualizados sin depender de un desarrollador.
*Criterio:* los cambios se reflejan de inmediato y quedan con fecha y responsable.

**HU-19 (RF-22).** Como administrador, quiero gestionar rutas, contactos, horarios y mensajes de urgencia, para mantenerlos vigentes.
*Criterio:* el administrador actualiza los datos y el portal muestra la versión vigente.

**HU-20 (RF-23).** Como administrador, quiero crear y administrar cuestionarios (preguntas, opciones, reglas de nivel, recomendaciones) con control de versiones, para cubrir distintas temáticas sin tocar código.
*Criterio:* una versión en uso no se altera retroactivamente; un cambio genera una nueva versión.

**HU-21 (RF-24).** Como administrador técnico, quiero administrar usuarios y roles autorizados, para controlar quién tiene acceso administrativo.
*Criterio:* solo el administrador técnico asigna o revoca roles y cada cambio queda auditado.

**HU-22 (RF-25).** Como administrador técnico, quiero que los cambios administrativos queden en una bitácora, para poder rastrear qué se modificó, quién y cuándo.
*Criterio:* la bitácora registra actor, acción, fecha y objeto, sin respuestas sensibles.

### M07 — Analítica agregada (versión simplificada)

**HU-23 (RF-26).** Como administrador, quiero consultar estadísticas básicas agregadas de uso, para identificar qué se consulta más.
*Criterio:* no se muestra ninguna agrupación con menos de cinco registros.

**HU-24 (RF-27).** Como administrador, quiero exportar las estadísticas agregadas en formato simple (CSV), para compartirlas institucionalmente.
*Criterio:* la exportación respeta el umbral mínimo y no incluye identificadores individuales.

**HU-25 (RF-28).** Como visitante, quiero valorar si un contenido o ruta me fue útil, para contribuir a mejorar el portal.
*Criterio:* la valoración es opcional, no pide datos sensibles y solo alimenta estadísticas agregadas.

### M08 — Auditoría y soporte

**HU-26 (RF-29).** Como administrador técnico, quiero que los errores se muestren y registren de forma segura, para diagnosticar sin exponer información sensible.
*Criterio:* el usuario ve un mensaje comprensible; el registro excluye datos sensibles.

**HU-27 (RF-30).** Como administrador, quiero actualizar parámetros operativos (contactos, categorías, avisos) sin tocar código, para mantener el sistema al día por mi cuenta.
*Criterio:* estos parámetros se actualizan desde administración con los permisos correspondientes.

## Product Backlog priorizado (según el cronograma de 2.5 semanas)

| ID | Historia | Módulo | Días | Prioridad |
|---|---|---|---|---|
| HU-16 | Login/logout admin | M05 | 1-3 | Alta |
| HU-17 | Permisos por rol | M05 | 1-3 | Alta |
| HU-18 | CRUD contenidos/categorías | M06 | 4-5 | Alta |
| HU-19 | CRUD rutas/contactos | M06 | 4-5 | Alta |
| HU-27 | Parámetros operativos | M06 | 4-5 | Alta |
| HU-20 | Administración de cuestionarios | M06 | 6-8 | Alta |
| HU-08 | Aviso previo autoorientación | M03 | 6-8 | Alta |
| HU-09 | Formulario de autoorientación | M03 | 6-8 | Alta |
| HU-10 | Cálculo de nivel | M03 | 6-8 | Alta |
| HU-11 | Resumen del resultado | M03 | 6-8 | Alta |
| HU-12 | Recomendaciones y rutas | M03 | 6-8 | Alta |
| HU-13 | Anonimato garantizado | M03 | 6-8 | Alta |
| HU-01 | Consulta sin sesión | M01 | 9-10 | Alta |
| HU-02 | Categorías temáticas | M01 | 9-10 | Alta |
| HU-03 | Búsqueda por palabra clave | M01 | 9-10 | Alta |
| HU-04 | Contenido de señales de alerta | M01 | 9-10 | Alta |
| HU-05 | Consulta de rutas | M02 | 9-10 | Alta |
| HU-06 | Información urgente visible | M02 | 9-10 | Alta |
| HU-07 | Canal institucional directo | M02 | 9-10 | Alta |
| HU-14 | Contenido para docentes | M04 | 9-10 | Media |
| HU-15 | Límites del rol docente | M04 | 9-10 | Media |
| HU-22 | Bitácora de cambios | M08 | 9-10 | Alta |
| HU-21 | Gestión de usuarios/roles | M06 | 11-13 | Alta |
| HU-23 | Estadísticas agregadas | M07 | 11-13 | Media |
| HU-24 | Exportar estadísticas | M07 | 11-13 | Media |
| HU-25 | Valoración de utilidad | M07 | 11-13 | Media |
| HU-26 | Errores técnicos seguros | M08 | 11-13 | Alta |

**Nota:** dentro de cada bloque de días, programar primero las de prioridad Alta. Si algo se atrasa, se sacrifica tiempo de pulido en las de prioridad Media, nunca en las Altas.
