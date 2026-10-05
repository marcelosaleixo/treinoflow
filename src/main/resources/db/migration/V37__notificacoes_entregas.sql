CREATE TABLE IF NOT EXISTS notificacoes_entregas (
    id BIGSERIAL PRIMARY KEY,
    notificacao_id BIGINT NOT NULL,
    canal VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    tentativas INTEGER NOT NULL DEFAULT 0,
    destino VARCHAR(180),
    data_envio TIMESTAMP,
    proxima_tentativa TIMESTAMP,
    ultimo_erro VARCHAR(1000),
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_notificacao_canal UNIQUE (notificacao_id, canal),
    CONSTRAINT fk_entrega_notificacao FOREIGN KEY (notificacao_id) REFERENCES notificacoes_treinoflow(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_entrega_status ON notificacoes_entregas(status);
CREATE INDEX IF NOT EXISTS idx_entrega_tentativa ON notificacoes_entregas(proxima_tentativa);
