-- Etapa 43: Portal do Aluno e acompanhamento de execução
ALTER TABLE alunos ADD COLUMN IF NOT EXISTS token_portal VARCHAR(100);
CREATE UNIQUE INDEX IF NOT EXISTS uk_alunos_token_portal ON alunos(token_portal) WHERE token_portal IS NOT NULL;

CREATE TABLE IF NOT EXISTS registros_treino_aluno (
    id BIGSERIAL PRIMARY KEY,
    treino_id BIGINT NOT NULL,
    aluno_id BIGINT NOT NULL,
    data_execucao DATE NOT NULL,
    concluido BOOLEAN NOT NULL DEFAULT TRUE,
    nota INTEGER,
    feedback TEXT,
    data_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_registro_treino_data UNIQUE (treino_id, data_execucao),
    CONSTRAINT fk_registro_treino FOREIGN KEY (treino_id) REFERENCES treinos(id),
    CONSTRAINT fk_registro_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id),
    CONSTRAINT ck_registro_nota CHECK (nota IS NULL OR (nota BETWEEN 1 AND 5))
);
CREATE INDEX IF NOT EXISTS idx_registro_aluno_data ON registros_treino_aluno(aluno_id, data_execucao);
CREATE INDEX IF NOT EXISTS idx_registro_treino_data ON registros_treino_aluno(treino_id, data_execucao);
