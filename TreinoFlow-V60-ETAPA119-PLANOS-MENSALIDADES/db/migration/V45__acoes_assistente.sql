CREATE TABLE acoes_assistente (
    id BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL,
    aluno_id BIGINT NOT NULL,
    tipo_acao VARCHAR(30) NOT NULL,
    resultado VARCHAR(30) NOT NULL DEFAULT 'EM_ACOMPANHAMENTO',
    score_risco INTEGER NOT NULL,
    descricao_acao VARCHAR(500) NOT NULL,
    mensagem TEXT,
    executada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resultado_em TIMESTAMP,
    CONSTRAINT fk_acao_assistente_personal FOREIGN KEY (personal_id) REFERENCES usuarios_personal(id),
    CONSTRAINT fk_acao_assistente_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id)
);
CREATE INDEX idx_acao_assistente_personal_data ON acoes_assistente(personal_id, executada_em);
CREATE INDEX idx_acao_assistente_aluno_data ON acoes_assistente(aluno_id, executada_em);
