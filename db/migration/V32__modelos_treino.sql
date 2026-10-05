CREATE TABLE IF NOT EXISTS modelos_treino (
    id BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL,
    nome VARCHAR(80) NOT NULL,
    descricao TEXT,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_modelo_treino_personal FOREIGN KEY (personal_id) REFERENCES usuarios_personais(id)
);
CREATE INDEX IF NOT EXISTS idx_modelo_treino_personal_nome ON modelos_treino(personal_id, nome);
CREATE TABLE IF NOT EXISTS modelos_treino_exercicios (
    id BIGSERIAL PRIMARY KEY,
    modelo_id BIGINT NOT NULL,
    exercicio_id BIGINT NOT NULL,
    ordem INTEGER NOT NULL,
    series INTEGER,
    repeticoes VARCHAR(30),
    carga VARCHAR(30),
    descanso_segundos INTEGER,
    observacao TEXT,
    CONSTRAINT fk_modelo_exercicio_modelo FOREIGN KEY (modelo_id) REFERENCES modelos_treino(id) ON DELETE CASCADE,
    CONSTRAINT fk_modelo_exercicio_exercicio FOREIGN KEY (exercicio_id) REFERENCES exercicios(id),
    CONSTRAINT uk_modelo_treino_exercicio_ordem UNIQUE (modelo_id, ordem)
);
