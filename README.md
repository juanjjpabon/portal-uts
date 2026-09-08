# Portal de orientación psicosocial UTS

Proyecto 65-2026-015. Spring Boot + Spring Security + PostgreSQL + Thymeleaf/Bootstrap.

## Estado

| Historia | Descripción | Estado |
|---|---|---|
| HU-16 | Login / logout de administrador | Implementada |
| HU-17 | Permisos por rol (separación estricta funcional / técnico) | Implementada |

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

   Flyway crea el esquema (`V1`) y siembra los roles (`V2`). `SeedAdminInicial` crea el
   usuario `admin.tecnico@uts.edu.co` con rol `ADMIN_TECNICO` usando
   `PORTAL_ADMIN_INICIAL_PASSWORD`.

4. Abrir <http://localhost:8080> → **Ingresar**.

## Rutas

| Ruta | Acceso |
|---|---|
| `/`, `/contenidos/**`, `/rutas/**`, `/autoorientacion/**`, `/buscar` | Público |
| `/login`, `/logout` | Público |
| `/admin` | Cualquier administrador autenticado |
| `/admin/contenidos`, `/rutas`, `/cuestionarios`, `/parametros`, `/analitica` | Solo `ADMIN_FUNCIONAL` |
| `/admin/usuarios`, `/bitacora`, `/sistema` | Solo `ADMIN_TECNICO` |

## Pruebas

```powershell
./mvnw test
```

`SeguridadWebTest` cubre HU-16 y HU-17 a nivel de URL (no requiere base de datos).

## Contraseña de BD en archivo local (opcional)

Si prefieres no usar variables de entorno, crea `src/main/resources/application-local.yml`
(ignorado por git) con:

```yaml
spring:
  datasource:
    password: tu-clave
```

y arranca con `./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev,local"`.
