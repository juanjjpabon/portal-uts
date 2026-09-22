# Portal de orientación psicosocial UTS

Proyecto 65-2026-015. Spring Boot + Spring Security + PostgreSQL + Thymeleaf/Bootstrap.

## Estado

| Historia | Descripción | Estado |
|---|---|---|
| HU-16 | Login / logout de administrador | Implementada |
| HU-17 | Permisos por rol (separación estricta funcional / técnico) | Implementada |
| HU-18 | CRUD de contenidos y categorías | Implementada |
| HU-19 | CRUD de rutas, contactos, horarios y mensajes de urgencia | Implementada |
| HU-27 | Parámetros operativos (contactos, categorías, avisos) | Implementada |
| HU-20 | Administración de cuestionarios con control de versiones | Implementada (panel) |
| HU-21 | Gestión de usuarios y roles (exclusiva de `ADMIN_TECNICO`) | Implementada |
| HU-08–HU-13 | Autoorientación pública (M03) | Implementada |
| HU-06 / HU-07 | Información urgente / canal institucional | Implementada |
| HU-01 a HU-05 | Contenidos y rutas públicos, búsqueda (M01/M02) | Implementada |
| HU-22 | Bitácora de cambios: visor global + todos los módulos conectados | Implementada |
| HU-26 | Errores técnicos seguros | Implementada |
| HU-14 / HU-15 | Contenido para docentes (M04, categoría de contenido) | Sin cambio de código; categoría **"Docentes"** creada e inactiva, a la espera del texto de la directora/CAE |
| HU-23 / HU-24 | Estadísticas agregadas y exportación CSV (M07) | Implementada |
| HU-25 | Valoración de utilidad (M07) | Implementada |

HU-18 y HU-19 comparten el modelo genérico `Recurso` (`tipo` ∈ CONTENIDO / RUTA /
CONTACTO, F-DC-125): una entidad, un repositorio, un servicio y un par de plantillas.
`Categoria` es entidad aparte, N:M con `Recurso`.

HU-27: contactos y categorías ya quedan cubiertos por HU-18/HU-19; lo que añade es
`Parametro` (catálogo fijo clave/valor, editable en `/admin/parametros`) para los
avisos de autoorientación (HU-08/HU-11), el mensaje de urgencia (HU-06) y el canal
institucional (HU-07). `ParametroService.valor(clave)` es el punto de consumo.

M01/M02 (`PortalPublicoService`, dentro de `contenido`): capa de solo lectura sobre
`Recurso`/`Categoria`, siempre filtrando `estado = PUBLICADO` — un `BORRADOR` o
`ARCHIVADO` no aparece en ningún listado ni es accesible por su slug (404). `/contenidos`
(chips de categoría, HU-02) y `/contenidos/{slug}` (HU-04, con fecha de actualización y
fuente); `/rutas` combina `RUTA` y `CONTACTO` en una sola página (HU-05); `/buscar`
(HU-03) es un único cuadro en el navbar que busca en título+resumen+cuerpo para
contenidos y título+dependencia+canal para rutas/contactos, agrupado por tipo. Sin
cambios de esquema ni de seguridad (ya estaba en `permitAll`).

M03 (módulo `autoorientacion`): flujo público y **anónimo** — `/autoorientacion` (temas
disponibles) → aviso previo con el parámetro `aviso.autoorientacion.previo` (HU-08) →
formulario de una sola página (HU-09) → `POST .../resultado` que calcula el nivel con
`CalculadoraNivel` sobre la **versión PUBLICADA** (suma de puntajes → banda `[min,max]`,
HU-10) y muestra explicación + aviso no-diagnóstico + recomendaciones + rutas (HU-11/12).
`/urgencia` (HU-06) y el botón de canal institucional (HU-07) usan los parámetros de HU-27.
**HU-13**: ninguna entidad ni tabla nueva, sin `HttpSession`, sin bitácora — las
respuestas viajan en el POST y se descartan; el resultado se calcula en memoria.
Migración `V7` sólo añade `slug` a `cuestionario` para las URLs públicas.

HU-21 (módulo `identidad` + `bitacora`): CRUD de usuarios en `/admin/usuarios` sólo para
`ADMIN_TECNICO`. Crear pide nombre/correo/contraseña inicial y marca `debeCambiarClave`;
un interceptor obliga a cambiarla en el primer ingreso (`/cuenta/contrasena`). Reglas
anti-bloqueo: nadie se desactiva ni se quita `ADMIN_TECNICO` a sí mismo, y siempre debe
quedar ≥1 `ADMIN_TECNICO` activo. Cada cambio (crear, editar, rol asignado/revocado,
activar/desactivar, restablecer/cambiar contraseña) se registra en `registro_bitacora`
(actor, acción, fecha, objeto — nunca la contraseña). El `admin.funcional` creado a mano
por SQL ya se gestiona desde esta pantalla como cualquier otro. Migración `V6`.

HU-22 (pasada de pulido, módulo `bitacora`): visor global en `/admin/bitacora` (solo
`ADMIN_TECNICO`; `BitacoraService.registrar` en sí queda sin restringir porque lo llaman
también los servicios de `ADMIN_FUNCIONAL`), con filtro por tipo de objeto (dropdown
poblado con `SELECT DISTINCT`, nunca hardcodeado) y rango de fecha, paginado. `Recurso`,
`Categoria`, `Cuestionario`/versión y `Parametro` ya llaman a la bitácora en cada
mutación (granularidad gruesa: preguntas/opciones/niveles/recomendaciones en un
cuestionario `BORRADOR` no generan fila propia). El filtro combinado usa
`Specification`/`JpaSpecificationExecutor` en vez de un `@Query` con `is null`: un
`Instant` nulo que solo se compara contra `IS NULL` hace que Postgres no pueda inferirle
tipo (misma familia del bug `lower(bytea)` de HU-18) — con `Specification` un filtro
ausente simplemente no agrega parámetro.

HU-26 (pasada de pulido, `common.web.PortalErrorAttributes`): `server.error.include-*`
fijado en `never`/`false` en `application.yml` para que ninguna respuesta de `/error`
lleve stack trace, excepción o mensaje interno, sea cual sea el `Accept` del cliente
(HTML o JSON). Un componente que extiende `DefaultErrorAttributes` intercepta todo lo
que pasa por `/error`; para un `status >= 500` genera un código de referencia corto,
lo agrega al modelo (se muestra en `error/500.html`/`5xx.html`) y registra en el log a
nivel `ERROR` la excepción completa con su stack trace, más método y ruta originales —
nunca los parámetros de la petición, para no dejar contraseñas de `/login` o
`/cuenta/contrasena` en el log. `error/4xx.html` y `5xx.html` son plantillas de
respaldo para cualquier código sin plantilla propia. Verificado en vivo con un 500 real
(bug de tipado de parámetros antes de la corrección con `Specification`): quedó el
código de referencia en pantalla y el stack trace completo en el log.

HU-20 (módulo `cuestionario`): `Cuestionario → CuestionarioVersion → Pregunta → Opcion`
y `CuestionarioVersion → NivelResultado → Recomendacion`, con `NivelResultado ↔ Recurso(RUTA)`
N:M. Una versión `BORRADOR` es editable; al publicarse queda inmutable y "un cambio
genera una nueva versión" (copia profunda a un borrador nuevo). El nivel de resultado
se calcula por bandas de puntaje `[min,max]` sobre la suma de las opciones elegidas.
`ValidadorVersion` bloquea la publicación si faltan opciones, hay huecos/solapes de
puntaje o algún nivel no tiene recomendación y ruta. Panel en `/admin/cuestionarios`.

M07 (módulo `analitica`, + contadores en `contenido`/`cuestionario`): **sin ningún
registro por evento** — todo son contadores `int` en entidades que ya existían,
incrementados con un `UPDATE` atómico (nunca lectura-modificación-escritura):
`Recurso.vistas` (cada apertura de `/contenidos/{slug}`), `Recurso.valoracionesUtil`/
`.valoracionesNoUtil` (HU-25, botón "¿Te fue útil?" en el detalle de un contenido y en
cada tarjeta de `/rutas`) y `NivelResultado.vecesObtenido` (HU-23, se suma cuando
`AutoorientacionService.calcular` determina un nivel — sigue sin guardar la respuesta,
HU-13). `AnaliticaService` arma el reporte de `/admin/analitica` y el CSV de
`/admin/analitica/exportar.csv` (BOM UTF-8, formato "tidy" `seccion,nombre,indicador,valor`)
con el **mismo filtro `>= 5`** en las tres secciones (contenidos más consultados,
autoorientación por nivel, valoraciones) — una agrupación por debajo del umbral no
sale ni en pantalla ni en el CSV, en ambos casos porque nunca se calcula, no porque se
oculte después. El voto de HU-25 no identifica al visitante (sin cookie, sin sesión
propia) y vuelve a la página de origen validando el header `Referer` contra una lista
blanca de rutas propias, nunca redirigiendo a la URL cruda del encabezado. Migración `V8`.

Pendiente del backlog: el **texto** de HU-14/HU-15 (la categoría y el resto de la
infraestructura ya están listos, ver arriba).

## Requisitos

- Java 21
- PostgreSQL 18 en `localhost:5432`, usuario `postgres` (ya instalado)
- Maven no hace falta: se usa el wrapper (`./mvnw`)

## Puesta en marcha

1. **Crear la base de datos** (una sola vez):

   ```sql
   CREATE DATABASE portal_uts;
   ```

2. **Definir las variables de entorno** antes de arrancar:

   | Variable | Para qué |
   |---|---|
   | `DB_PASSWORD` | Contraseña del usuario `postgres` |
   | `PORTAL_ADMIN_INICIAL_PASSWORD` | Contraseña del primer administrador técnico (solo se usa si la tabla `usuario` está vacía) |

   PowerShell:
   ```powershell
   $env:DB_PASSWORD = "..."
   $env:PORTAL_ADMIN_INICIAL_PASSWORD = "..."
   ```

3. **Arrancar**:

   ```powershell
   ./mvnw spring-boot:run
   ```

   Flyway crea el esquema (`V1`–`V8`) y siembra roles (`V2`) y parámetros (`V4`). `SeedAdminInicial`
   crea el usuario `admin.tecnico@uts.edu.co` con rol `ADMIN_TECNICO` usando
   `PORTAL_ADMIN_INICIAL_PASSWORD`.

4. Abrir <http://localhost:8080> → **Ingresar**.

### Crear los demás usuarios

Desde HU-21, el administrador técnico crea el resto de usuarios (incluido el
`ADMIN_FUNCIONAL`) en **`/admin/usuarios` → Nuevo usuario**. Ya no hace falta SQL manual;
cada usuario nuevo debe cambiar su contraseña inicial en el primer ingreso.

*(En bases anteriores a la V6 el `admin.funcional@uts.edu.co` se había creado a mano por
SQL; ya se administra como cualquier otro.)*

## Rutas

| Ruta | Acceso |
|---|---|
| `/`, `/contenidos/**`, `/rutas/**`, `/autoorientacion/**`, `/urgencia`, `/buscar`, `/valoraciones/**` | Público |
| `/login`, `/logout` | Público |
| `/admin` | Cualquier administrador autenticado |
| `/admin/recursos/{contenidos\|rutas\|contactos}/**`, `/admin/categorias/**` | Solo `ADMIN_FUNCIONAL` |
| `/admin/cuestionarios`, `/parametros`, `/analitica` | Solo `ADMIN_FUNCIONAL` |
| `/admin/usuarios`, `/admin/bitacora`, `/admin/sistema` | Solo `ADMIN_TECNICO` |
| `/cuenta/contrasena` | Cualquier usuario autenticado (cambio de la propia contraseña) |

## Pruebas

```powershell
./mvnw test
```

Sin base de datos: `SeguridadWebTest` (HU-16/HU-17 por URL), `CuentaServiceTest`
(cambio de contraseña obligatorio vs. voluntario), `RecursoAdminControllerTest`
(HU-18/HU-19), `RecursoFormValidacionTest` (validación por tipo), `ParametroAdminControllerTest`
y `ParametroServiceTest` (HU-27), `CuestionarioAdminControllerTest` y `ValidadorVersionTest`
(HU-20: workflow y reglas de publicación), `UsuarioServiceTest`, `UsuarioAdminControllerTest`
y `BitacoraServiceTest` (HU-21/HU-22: anti-bloqueo, roles, auditoría),
`CalculadoraNivelTest`, `AutoorientacionServiceTest` y `AutoorientacionControllerTest`
(M03: cálculo del nivel, anonimato, flujo público), `PortalPublicoServiceTest` y
`PortalPublicoControllerTest` (M01/M02: solo contenido publicado, búsqueda),
`RecursoServiceTest`, `CategoriaServiceTest`, `CuestionarioServiceTest` (HU-22: cada
mutación audita la acción correcta), `BitacoraAdminControllerTest` (visor global y
permisos), `PortalErrorAttributesTest` (HU-26: sin trace/exception/message, con
referencia solo en `≥500`), `AnaliticaServiceTest` y `AnaliticaAdminControllerTest`
(HU-23/24: umbral `>=5`, CSV = mismas filas que el reporte), `ValoracionServiceTest`
y `ValoracionPublicaControllerTest` (HU-25: solo en `PUBLICADO`, redirect por
`Referer` con lista blanca), `SlugsTest`. 121 pruebas en total.

## Contraseña de BD en archivo local (opcional)

Si prefieres no usar variables de entorno, crea `src/main/resources/application-local.yml`
(ignorado por git) con:

```yaml
spring:
  datasource:
    password: tu-clave
```

y arranca con `./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev,local"`.
