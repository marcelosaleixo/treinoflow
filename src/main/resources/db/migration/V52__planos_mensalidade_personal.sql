-- ETAPA 119 - Planos e mensalidades recorrentes do Personal.
CREATE TABLE IF NOT EXISTS planos_mensalidade_personal (
    id BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL REFERENCES usuarios_personais(id),
    aluno_id BIGINT NOT NULL REFERENCES alunos(id),
    nome VARCHAR(120) NOT NULL,
    valor NUMERIC(12,2) NOT NULL,
    dia_vencimento INTEGER NOT NULL,
    data_inicio DATE NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    observacao VARCHAR(500),
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_plano_mensalidade_personal_ativo ON planos_mensalidade_personal(personal_id,ativo);
CREATE INDEX IF NOT EXISTS idx_plano_mensalidade_aluno ON planos_mensalidade_personal(aluno_id);
ALTER TABLE contas_receber_personal ADD COLUMN IF NOT EXISTS plano_mensalidade_id BIGINT;
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_conta_receber_plano_mensalidade') THEN
        ALTER TABLE contas_receber_personal ADD CONSTRAINT fk_conta_receber_plano_mensalidade FOREIGN KEY (plano_mensalidade_id) REFERENCES planos_mensalidade_personal(id);
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_conta_receber_plano_mensalidade ON contas_receber_personal(plano_mensalidade_id);
