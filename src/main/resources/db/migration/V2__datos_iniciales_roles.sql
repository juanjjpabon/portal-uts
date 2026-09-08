-- Catalogo fijo de roles (HU-17). El administrador tecnico asigna estos roles a los
-- usuarios (HU-21) pero no crea tipos nuevos.

INSERT INTO rol (nombre, descripcion) VALUES
    ('ADMIN_FUNCIONAL',
     'Gestiona contenidos, categorias, rutas, contactos, cuestionarios, parametros y analitica.'),
    ('ADMIN_TECNICO',
     'Gestiona usuarios y roles, bitacora y configuracion tecnica del portal.');

-- El usuario administrador inicial NO se crea aqui: lo crea SeedAdminInicial al
-- arrancar, tomando la contrasena de la variable de entorno
-- PORTAL_ADMIN_INICIAL_PASSWORD (para no dejar credenciales en el repositorio).
