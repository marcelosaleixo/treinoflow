CREATE TABLE IF NOT EXISTS metas_retencao (
    id BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL,
    mes_referencia DATE NOT NULL,
    meta_recuperacoes INTEGER NOT NULL DEFAULT 5,
    meta_taxa DOUBLE PRECISION NOT NULL DEFAULT 60,
    CONSTRAINT uk_meta_retencao_personal_mes UNIQUE (personal_id, mes_referencia),
    CONSTRAINT fk_meta_retencao_personal FOREIGN KEY (personal_id) REFERENCES usuarios_personais(id)
);
CREATE INDEX IF NOT EXISTS idx_meta_retencao_personal ON metas_retencao(personal_id, mes_referencia);
