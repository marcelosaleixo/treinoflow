CREATE TABLE IF NOT EXISTS cobrancas_treinoflow (
    id BIGSERIAL PRIMARY KEY,
    assinatura_id BIGINT NOT NULL,
    competencia DATE NOT NULL,
    valor NUMERIC(10,2) NOT NULL,
    data_vencimento DATE NOT NULL,
    data_pagamento DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    observacao VARCHAR(500),
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cobranca_assinatura_competencia UNIQUE (assinatura_id, competencia),
    CONSTRAINT fk_cobranca_assinatura FOREIGN KEY (assinatura_id) REFERENCES assinaturas_treinoflow(id)
);
CREATE INDEX IF NOT EXISTS idx_cobranca_status ON cobrancas_treinoflow(status);
CREATE INDEX IF NOT EXISTS idx_cobranca_vencimento ON cobrancas_treinoflow(data_vencimento);
