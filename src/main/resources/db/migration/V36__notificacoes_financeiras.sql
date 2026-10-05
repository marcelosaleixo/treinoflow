CREATE TABLE IF NOT EXISTS notificacoes_treinoflow (
    id BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL,
    cobranca_id BIGINT,
    tipo VARCHAR(40) NOT NULL,
    titulo VARCHAR(140) NOT NULL,
    mensagem VARCHAR(500) NOT NULL,
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    chave_unica VARCHAR(180) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_leitura TIMESTAMP NULL,
    CONSTRAINT uk_notificacao_chave UNIQUE (chave_unica),
    CONSTRAINT fk_notificacao_personal FOREIGN KEY (personal_id) REFERENCES usuarios_personais(id),
    CONSTRAINT fk_notificacao_cobranca FOREIGN KEY (cobranca_id) REFERENCES cobrancas_treinoflow(id)
);
CREATE INDEX IF NOT EXISTS idx_notificacao_personal_lida ON notificacoes_treinoflow(personal_id, lida);
CREATE INDEX IF NOT EXISTS idx_notificacao_criacao ON notificacoes_treinoflow(data_criacao);
