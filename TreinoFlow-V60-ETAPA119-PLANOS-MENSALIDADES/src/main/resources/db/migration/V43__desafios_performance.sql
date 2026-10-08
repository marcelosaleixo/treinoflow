CREATE TABLE IF NOT EXISTS desafios_performance (
    id BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL,
    mes_referencia DATE NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    titulo VARCHAR(120) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    meta INTEGER NOT NULL,
    bonus_pontos INTEGER NOT NULL DEFAULT 0,
    concluido_em TIMESTAMP NULL,
    CONSTRAINT uk_desafio_performance_personal_mes_tipo UNIQUE (personal_id, mes_referencia, tipo),
    CONSTRAINT fk_desafio_performance_personal FOREIGN KEY (personal_id) REFERENCES usuarios_personais(id)
);
CREATE INDEX IF NOT EXISTS idx_desafio_performance_personal_mes ON desafios_performance(personal_id, mes_referencia);
