-- Etapa de experiência de treino: registro por série, RPE e histórico por série.
CREATE TABLE IF NOT EXISTS execucoes_series (
    id BIGSERIAL PRIMARY KEY,
    registro_id BIGINT NOT NULL,
    treino_exercicio_id BIGINT NOT NULL,
    numero_serie INTEGER NOT NULL,
    carga_realizada VARCHAR(40),
    repeticoes_realizadas INTEGER,
    rpe INTEGER,
    observacao TEXT,
    concluido BOOLEAN NOT NULL DEFAULT TRUE,
    data_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_execucao_serie_registro_item_numero UNIQUE (registro_id, treino_exercicio_id, numero_serie),
    CONSTRAINT fk_execucao_serie_registro FOREIGN KEY (registro_id) REFERENCES registros_treino_aluno(id),
    CONSTRAINT fk_execucao_serie_item FOREIGN KEY (treino_exercicio_id) REFERENCES treino_exercicios(id),
    CONSTRAINT ck_execucao_serie_numero CHECK (numero_serie > 0),
    CONSTRAINT ck_execucao_serie_rpe CHECK (rpe IS NULL OR (rpe BETWEEN 1 AND 10))
);
CREATE INDEX IF NOT EXISTS idx_execucao_serie_registro ON execucoes_series(registro_id);
CREATE INDEX IF NOT EXISTS idx_execucao_serie_item ON execucoes_series(treino_exercicio_id);
