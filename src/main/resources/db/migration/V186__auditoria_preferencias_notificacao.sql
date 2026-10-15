-- Etapa 186: histórico auditável das alterações de preferências por Personal.
CREATE TABLE IF NOT EXISTS auditoria_preferencias_notificacao (
    id BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    valor_anterior BOOLEAN NOT NULL,
    valor_novo BOOLEAN NOT NULL,
    alterado_em TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auditoria_preferencia_personal
        FOREIGN KEY (personal_id) REFERENCES usuarios_personais(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_auditoria_preferencia_personal_data
    ON auditoria_preferencias_notificacao (personal_id, alterado_em DESC);
CREATE INDEX IF NOT EXISTS idx_auditoria_preferencia_tipo
    ON auditoria_preferencias_notificacao (tipo);
