ALTER TABLE automacoes_retencao_config ADD COLUMN IF NOT EXISTS valor_mensal_aluno_estimado NUMERIC(10,2) NOT NULL DEFAULT 0;
ALTER TABLE automacoes_retencao_config ADD COLUMN IF NOT EXISTS custo_automacao_mensal NUMERIC(10,2) NOT NULL DEFAULT 0;
