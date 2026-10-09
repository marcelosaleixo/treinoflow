CREATE TABLE IF NOT EXISTS metas_receita_acoes_crm (
    id BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL,
    mes_referencia DATE NOT NULL,
    valor_meta NUMERIC(12,2) NOT NULL DEFAULT 0,
    observacao VARCHAR(500),
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meta_receita_acoes_personal FOREIGN KEY (personal_id) REFERENCES usuarios_personais(id),
    CONSTRAINT uk_meta_receita_acoes_personal_mes UNIQUE (personal_id, mes_referencia),
    CONSTRAINT ck_meta_receita_acoes_valor CHECK (valor_meta >= 0)
);
