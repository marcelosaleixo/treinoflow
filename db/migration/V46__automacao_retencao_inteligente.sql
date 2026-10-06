CREATE TABLE IF NOT EXISTS automacoes_retencao_config (
    id BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL UNIQUE,
    ativa BOOLEAN NOT NULL DEFAULT FALSE,
    whatsapp_critico BOOLEAN NOT NULL DEFAULT TRUE,
    followup_alto BOOLEAN NOT NULL DEFAULT TRUE,
    score_minimo INTEGER NOT NULL DEFAULT 50,
    max_acoes_dia INTEGER NOT NULL DEFAULT 10,
    cooldown_dias INTEGER NOT NULL DEFAULT 7,
    hora_inicio VARCHAR(5) NOT NULL DEFAULT '08:00',
    hora_fim VARCHAR(5) NOT NULL DEFAULT '20:00',
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_automacao_retencao_personal FOREIGN KEY (personal_id) REFERENCES usuarios_personais(id)
);

ALTER TABLE acoes_assistente ADD COLUMN IF NOT EXISTS automatica BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE acoes_assistente ADD COLUMN IF NOT EXISTS regra_automacao VARCHAR(50);
CREATE INDEX IF NOT EXISTS idx_acao_assistente_automatica ON acoes_assistente(personal_id, automatica, executada_em);
