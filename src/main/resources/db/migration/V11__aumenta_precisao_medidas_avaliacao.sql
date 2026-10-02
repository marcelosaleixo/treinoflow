-- Ajusta a precisão numérica dos campos de avaliação física existentes.
-- Compatível com PostgreSQL; preserva os valores já gravados.
ALTER TABLE avaliacoes_fisicas
    ALTER COLUMN peso_kg TYPE NUMERIC(6,2),
    ALTER COLUMN altura_metros TYPE NUMERIC(4,2),
    ALTER COLUMN percentual_gordura TYPE NUMERIC(5,2),
    ALTER COLUMN cintura_cm TYPE NUMERIC(6,2),
    ALTER COLUMN quadril_cm TYPE NUMERIC(6,2),
    ALTER COLUMN torax_cm TYPE NUMERIC(6,2),
    ALTER COLUMN braco_cm TYPE NUMERIC(6,2),
    ALTER COLUMN coxa_cm TYPE NUMERIC(6,2);
