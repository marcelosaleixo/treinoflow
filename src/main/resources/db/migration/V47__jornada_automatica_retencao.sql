ALTER TABLE acoes_assistente ADD COLUMN IF NOT EXISTS jornada_id VARCHAR(36);
ALTER TABLE acoes_assistente ADD COLUMN IF NOT EXISTS etapa_jornada INTEGER NOT NULL DEFAULT 0;
ALTER TABLE acoes_assistente ADD COLUMN IF NOT EXISTS proxima_acao_em TIMESTAMP;
ALTER TABLE acoes_assistente ADD COLUMN IF NOT EXISTS jornada_status VARCHAR(30);
CREATE INDEX IF NOT EXISTS idx_acao_assistente_jornada ON acoes_assistente(personal_id, jornada_id, proxima_acao_em);
