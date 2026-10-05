-- Etapa 44 - Evolução do aluno e registro real dos exercícios.
-- O projeto atual usa ddl-auto=update; esta migration documenta a estrutura esperada.
CREATE TABLE IF NOT EXISTS execucoes_exercicios (
    id BIGSERIAL PRIMARY KEY,
    registro_id BIGINT NOT NULL,
    treino_exercicio_id BIGINT NOT NULL,
    carga_realizada VARCHAR(40),
    repeticoes_realizadas INTEGER,
    observacao TEXT,
    concluido BOOLEAN NOT NULL DEFAULT TRUE,
    data_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_execucao_exercicio_registro_item UNIQUE (registro_id, treino_exercicio_id),
    CONSTRAINT fk_execucao_exercicio_registro FOREIGN KEY (registro_id) REFERENCES registros_treino_aluno(id),
    CONSTRAINT fk_execucao_exercicio_item FOREIGN KEY (treino_exercicio_id) REFERENCES treino_exercicios(id)
);
CREATE INDEX IF NOT EXISTS idx_execucao_exercicio_registro ON execucoes_exercicios(registro_id);
CREATE INDEX IF NOT EXISTS idx_execucao_exercicio_item ON execucoes_exercicios(treino_exercicio_id);
