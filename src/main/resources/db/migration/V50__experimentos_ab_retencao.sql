CREATE TABLE IF NOT EXISTS experimentos_retencao (
    id BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL REFERENCES usuarios_personais(id),
    faixa_risco VARCHAR(20) NOT NULL,
    tipo_acao VARCHAR(30) NOT NULL,
    variante_a TEXT NOT NULL,
    variante_b TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    encerrado_em TIMESTAMP NULL
);
CREATE INDEX IF NOT EXISTS idx_exp_ret_personal_status ON experimentos_retencao(personal_id,status);
CREATE INDEX IF NOT EXISTS idx_exp_ret_faixa_tipo ON experimentos_retencao(faixa_risco,tipo_acao);
ALTER TABLE acoes_assistente ADD COLUMN IF NOT EXISTS experimento_id BIGINT NULL REFERENCES experimentos_retencao(id);
ALTER TABLE acoes_assistente ADD COLUMN IF NOT EXISTS experimento_variante VARCHAR(1) NULL;
CREATE INDEX IF NOT EXISTS idx_acao_assistente_experimento ON acoes_assistente(experimento_id,experimento_variante);
