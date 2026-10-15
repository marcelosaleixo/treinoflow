-- Etapa 185: preferências individuais para avisos em tempo real.
-- A aplicação usa spring.jpa.hibernate.ddl-auto=update; este script é referência
-- para ambientes que adotem migrações SQL controladas. Revise antes de executar.
CREATE TABLE IF NOT EXISTS preferencias_notificacao (
    id BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    tempo_real_ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_preferencia_notificacao_personal
        FOREIGN KEY (personal_id) REFERENCES usuarios_personais(id),
    CONSTRAINT uk_preferencia_notificacao_personal_tipo
        UNIQUE (personal_id, tipo)
);
CREATE INDEX IF NOT EXISTS idx_preferencia_notificacao_personal
    ON preferencias_notificacao(personal_id);
