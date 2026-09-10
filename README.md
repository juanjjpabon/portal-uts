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
| HU-22 | Bitácora de cambios | Parcial: entidad + servicio + historial por usuario; visor global pendiente |
| HU-08–HU-13 | Autoorientación pública (M03) | Implementada |
| HU-06 / HU-07 | Información urgente / canal institucional | Implementada |

HU-18 y HU-19 comparten el modelo genérico `Recurso` (`tipo` ∈ CONTENIDO / RUTA /
CONTACTO, F-DC-125): una entidad, un repositorio, un servicio y un par de plantillas.
`Categoria` es entidad aparte, N:M con `Recurso`.

HU-27: contactos y categorías ya quedan cubiertos por HU-18/HU-19; lo que añade es
`Parametro` (catálogo fijo clave/valor, editable en `/admin/parametros`) para los
avisos de autoorientación (HU-08/HU-11), el mensaje de urgencia (HU-06) y el canal
institucional (HU-07). `ParametroService.valor(clave)` es el punto de consumo.

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

HU-20 (módulo `cuestionario`): `Cuestionario → CuestionarioVersion → Pregunta → Opcion`
y `CuestionarioVersion → NivelResultado → Recomendacion`, con `NivelResultado ↔ Recurso(RUTA)`
N:M. Una versión `BORRADOR` es editable; al publicarse queda inmutable y "un cambio
genera una nueva versión" (copia profunda a un borrador nuevo). El nivel de resultado
se calcula por bandas de puntaje `[min,max]` sobre la suma de las opciones elegidas.
`ValidadorVersion` bloquea la publicación si faltan opciones, hay huecos/solapes de
puntaje o algún nivel no tiene recomendación y ruta. Panel en `/admin/cuestionarios`.

El resto del backlog (M01–M08) se irá agregando módulo por módulo.

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

   Flyway crea el esquema (`V1`–`V7`) y siembra roles (`V2`) y parámetros (`V4`). `SeedAdminInicial`
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
| `/`, `/contenidos/**`, `/rutas/**`, `/autoorientacion/**`, `/urgencia`, `/buscar` | Público |
| `/login`, `/logout` | Público |
| `/admin` | Cualquier administrador autenticado |
| `/admin/recursos/{contenidos\|rutas\|contactos}/**`, `/admin/categorias/**` | Solo `ADMIN_FUNCIONAL` |
| `/admin/cuestionarios`, `/parametros`, `/analitica` | Solo `ADMIN_FUNCIONAL` |
| `/admin/usuarios`, `/bitacora`, `/sistema` | Solo `ADMIN_TECNICO` |
| `/cuenta/contrasena` | Cualquier usuario autenticado (cambio de la propia contraseña) |

## Pruebas

```powershell
./mvnw test
```

Sin base de datos: `SeguridadWebTest` (HU-16/HU-17 por URL), `RecursoAdminControllerTest`
(HU-18/HU-19), `RecursoFormValidacionTest` (validación por tipo), `ParametroAdminControllerTest`
y `ParametroServiceTest` (HU-27), `CuestionarioAdminControllerTest` y `ValidadorVersionTest`
(HU-20: workflow y reglas de publicación), `UsuarioServiceTest`, `UsuarioAdminControllerTest`
y `BitacoraServiceTest` (HU-21/HU-22: anti-bloqueo, roles, auditoría),
`CalculadoraNivelTest`, `AutoorientacionServiceTest` y `AutoorientacionControllerTest`
(M03: cálculo del nivel, anonimato, flujo público), `SlugsTest`. 63 pruebas en total.

## Contraseña de BD en archivo local (opcional)

Si prefieres no usar variables de entorno, crea `src/main/resources/application-local.yml`
(ignorado por git) con:

```yaml
spring:
  datasource:
    password: tu-clave
```

y arranca con `./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev,local"`.
