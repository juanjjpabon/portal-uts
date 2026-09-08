# Portal de orientación psicosocial UTS

Proyecto 65-2026-015. Spring Boot + Spring Security + PostgreSQL + Thymeleaf/Bootstrap.

## Estado

| Historia | Descripción | Estado |
|---|---|---|
| HU-16 | Login / logout de administrador | Implementada |
| HU-17 | Permisos por rol (separación estricta funcional / técnico) | Implementada |
| HU-18 | CRUD de contenidos y categorías | Implementada |
| HU-19 | CRUD de rutas, contactos, horarios y mensajes de urgencia | Implementada |

HU-18 y HU-19 comparten el modelo genérico `Recurso` (`tipo` ∈ CONTENIDO / RUTA /
CONTACTO, F-DC-125): una entidad, un repositorio, un servicio y un par de plantillas.
`Categoria` es entidad aparte, N:M con `Recurso`.

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

   Flyway crea el esquema (`V1`–`V3`) y siembra los roles (`V2`). `SeedAdminInicial`
   crea el usuario `admin.tecnico@uts.edu.co` con rol `ADMIN_TECNICO` usando
   `PORTAL_ADMIN_INICIAL_PASSWORD`.

4. Abrir <http://localhost:8080> → **Ingresar**.

### Usuario administrador funcional

HU-21 (gestión de usuarios) aún no existe, así que el `ADMIN_FUNCIONAL` que necesita
HU-18/HU-19 se crea a mano una vez:

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;
INSERT INTO usuario (nombre_completo, correo, hash_contrasena, activo, creado_en, creado_por)
VALUES ('Administrador Funcional', 'admin.funcional@uts.edu.co',
        crypt('CAMBIA-ESTA-CLAVE', gen_salt('bf', 10)), true, now(), 'sistema');
INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id FROM usuario u, rol r
WHERE u.correo = 'admin.funcional@uts.edu.co' AND r.nombre = 'ADMIN_FUNCIONAL';
```

## Rutas

| Ruta | Acceso |
|---|---|
| `/`, `/contenidos/**`, `/rutas/**`, `/autoorientacion/**`, `/buscar` | Público |
| `/login`, `/logout` | Público |
| `/admin` | Cualquier administrador autenticado |
| `/admin/recursos/{contenidos\|rutas\|contactos}/**`, `/admin/categorias/**` | Solo `ADMIN_FUNCIONAL` |
| `/admin/cuestionarios`, `/parametros`, `/analitica` | Solo `ADMIN_FUNCIONAL` |
| `/admin/usuarios`, `/bitacora`, `/sistema` | Solo `ADMIN_TECNICO` |

## Pruebas

```powershell
./mvnw test
```

Sin base de datos: `SeguridadWebTest` (HU-16/HU-17 por URL), `RecursoAdminControllerTest`
(HU-18/HU-19: CRUD y permisos), `RecursoFormValidacionTest` (validación por tipo) y
`SlugsTest`.

## Contraseña de BD en archivo local (opcional)

Si prefieres no usar variables de entorno, crea `src/main/resources/application-local.yml`
(ignorado por git) con:

```yaml
spring:
  datasource:
    password: tu-clave
```

y arranca con `./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev,local"`.
