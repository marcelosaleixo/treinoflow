CREATE TABLE IF NOT EXISTS metas_comerciais (
    id BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL,
    mes_referencia DATE NOT NULL,
    meta_alunos_ativos INTEGER NOT NULL DEFAULT 20,
    meta_treinos INTEGER NOT NULL DEFAULT 80,
    meta_recuperacoes INTEGER NOT NULL DEFAULT 5,
    CONSTRAINT uk_meta_comercial_personal_mes UNIQUE (personal_id, mes_referencia),
    CONSTRAINT fk_meta_comercial_personal FOREIGN KEY (personal_id) REFERENCES usuarios_personais(id)
);
CREATE INDEX IF NOT EXISTS idx_meta_comercial_personal ON metas_comerciais(personal_id);
