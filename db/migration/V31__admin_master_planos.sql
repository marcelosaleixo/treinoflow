-- TreinoFlow Etapa 31: perfis administrativos e planos comerciais
ALTER TABLE usuarios_personais ADD COLUMN IF NOT EXISTS perfil VARCHAR(20) NOT NULL DEFAULT 'PERSONAL';
ALTER TABLE usuarios_personais ADD COLUMN IF NOT EXISTS plano_id BIGINT;
CREATE TABLE IF NOT EXISTS planos_treinoflow (
 id BIGSERIAL PRIMARY KEY,
 nome VARCHAR(80) NOT NULL UNIQUE,
 descricao VARCHAR(500),
 valor_mensal NUMERIC(10,2) NOT NULL DEFAULT 0,
 limite_alunos INTEGER NOT NULL DEFAULT 10,
 ativo BOOLEAN NOT NULL DEFAULT TRUE,
 data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
DO $$ BEGIN
 IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name='fk_personal_plano') THEN
  ALTER TABLE usuarios_personais ADD CONSTRAINT fk_personal_plano FOREIGN KEY (plano_id) REFERENCES planos_treinoflow(id);
 END IF;
END $$;
