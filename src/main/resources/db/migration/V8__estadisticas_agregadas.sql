-- M07 HU-23/24/25: estadisticas basicas agregadas, sin registro por evento.
-- Contadores simples; nunca un intento ni un id de visitante.

ALTER TABLE recurso ADD COLUMN vistas INTEGER NOT NULL DEFAULT 0;
ALTER TABLE recurso ADD COLUMN valoraciones_util INTEGER NOT NULL DEFAULT 0;
ALTER TABLE recurso ADD COLUMN valoraciones_no_util INTEGER NOT NULL DEFAULT 0;

ALTER TABLE nivel_resultado ADD COLUMN veces_obtenido INTEGER NOT NULL DEFAULT 0;
