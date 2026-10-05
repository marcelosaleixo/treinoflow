-- Etapa 42 - CRM e recuperação de alunos.
-- O projeto atual usa Hibernate ddl-auto=update; este script documenta/aplica a estrutura
-- caso a instalação utilize migrations manuais.

CREATE TABLE IF NOT EXISTS interacoes_crm (
    id BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL,
    aluno_id BIGINT NOT NULL,
    canal VARCHAR(20) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    resultado VARCHAR(30) NOT NULL,
    assunto VARCHAR(160) NOT NULL,
    descricao TEXT NOT NULL,
    data_contato TIMESTAMP NOT NULL,
    data_proxima_acao DATE NULL,
    data_criacao TIMESTAMP NOT NULL,
    CONSTRAINT fk_interacao_crm_personal FOREIGN KEY (personal_id) REFERENCES usuarios_personais(id),
    CONSTRAINT fk_interacao_crm_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id)
);

CREATE INDEX IF NOT EXISTS idx_interacao_crm_personal_data ON interacoes_crm(personal_id, data_contato);
CREATE INDEX IF NOT EXISTS idx_interacao_crm_aluno_data ON interacoes_crm(aluno_id, data_contato);
CREATE INDEX IF NOT EXISTS idx_interacao_crm_proxima_acao ON interacoes_crm(data_proxima_acao);
