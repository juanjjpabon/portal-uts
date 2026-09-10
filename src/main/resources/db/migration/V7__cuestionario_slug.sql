-- M03: la autoorientacion publica usa un slug legible por cuestionario
-- (/autoorientacion/{slug}).

ALTER TABLE cuestionario ADD COLUMN slug VARCHAR(140);
UPDATE cuestionario SET slug = 'cuestionario-' || id WHERE slug IS NULL;
ALTER TABLE cuestionario ALTER COLUMN slug SET NOT NULL;
ALTER TABLE cuestionario ADD CONSTRAINT uq_cuestionario_slug UNIQUE (slug);
