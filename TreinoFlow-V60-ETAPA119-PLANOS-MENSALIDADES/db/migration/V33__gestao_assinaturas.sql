CREATE TABLE IF NOT EXISTS assinaturas_treinoflow (
 id BIGSERIAL PRIMARY KEY, personal_id BIGINT NOT NULL UNIQUE, plano_id BIGINT NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'ATIVA',
 data_inicio DATE NOT NULL DEFAULT CURRENT_DATE, data_vencimento DATE, data_proxima_cobranca DATE, data_cancelamento DATE, observacao VARCHAR(500), data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_assinatura_personal FOREIGN KEY(personal_id) REFERENCES usuarios_personais(id), CONSTRAINT fk_assinatura_plano FOREIGN KEY(plano_id) REFERENCES planos_treinoflow(id)
);
CREATE INDEX IF NOT EXISTS idx_assinatura_status ON assinaturas_treinoflow(status);
CREATE INDEX IF NOT EXISTS idx_assinatura_vencimento ON assinaturas_treinoflow(data_vencimento);
INSERT INTO assinaturas_treinoflow (personal_id, plano_id, status, data_inicio, data_vencimento, data_proxima_cobranca, data_atualizacao)
SELECT u.id, u.plano_id, CASE WHEN u.ativo THEN 'ATIVA' ELSE 'SUSPENSA' END, CURRENT_DATE, NULL, NULL, CURRENT_TIMESTAMP
FROM usuarios_personais u
WHERE u.perfil='PERSONAL' AND u.plano_id IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM assinaturas_treinoflow a WHERE a.personal_id=u.id);
