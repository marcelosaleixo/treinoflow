-- ETAPA 118 - Financeiro próprio do Personal Trainer.
-- O projeto atual usa Hibernate ddl-auto=update; este arquivo documenta a estrutura para futura migração controlada.
CREATE TABLE IF NOT EXISTS contas_receber_personal (
    id BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL REFERENCES usuarios_personais(id),
    aluno_id BIGINT REFERENCES alunos(id),
    descricao VARCHAR(160) NOT NULL,
    valor NUMERIC(12,2) NOT NULL,
    data_vencimento DATE NOT NULL,
    data_pagamento DATE,
    status VARCHAR(20) NOT NULL,
    forma_pagamento VARCHAR(30),
    observacao VARCHAR(500),
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_conta_receber_personal_status ON contas_receber_personal(personal_id,status);
CREATE INDEX IF NOT EXISTS idx_conta_receber_personal_vencimento ON contas_receber_personal(personal_id,data_vencimento);
