ALTER TABLE registros_treino_aluno ADD COLUMN IF NOT EXISTS esforco INTEGER;
ALTER TABLE registros_treino_aluno ADD COLUMN IF NOT EXISTS dor INTEGER;
ALTER TABLE registros_treino_aluno ADD COLUMN IF NOT EXISTS energia INTEGER;

ALTER TABLE registros_treino_aluno
    ADD CONSTRAINT ck_registro_feedback_esforco CHECK (esforco IS NULL OR esforco BETWEEN 1 AND 5);
ALTER TABLE registros_treino_aluno
    ADD CONSTRAINT ck_registro_feedback_dor CHECK (dor IS NULL OR dor BETWEEN 0 AND 10);
ALTER TABLE registros_treino_aluno
    ADD CONSTRAINT ck_registro_feedback_energia CHECK (energia IS NULL OR energia BETWEEN 1 AND 5);
