-- Execute uma vez no PostgreSQL antes de reiniciar a aplicação.
-- Corrige bancos existentes onde a coluna perfil ainda não existe ou contém NULL/vazio.
BEGIN;

ALTER TABLE usuarios_personais
    ADD COLUMN IF NOT EXISTS perfil varchar(20);

UPDATE usuarios_personais
SET perfil = 'PERSONAL'
WHERE perfil IS NULL OR btrim(perfil) = '';

ALTER TABLE usuarios_personais
    ALTER COLUMN perfil SET DEFAULT 'PERSONAL';

ALTER TABLE usuarios_personais
    ALTER COLUMN perfil SET NOT NULL;

COMMIT;
