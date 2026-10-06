CREATE TABLE IF NOT EXISTS agendamentos (
    id BIGSERIAL PRIMARY KEY,
    personal_id BIGINT NOT NULL,
    aluno_id BIGINT NOT NULL,
    inicio TIMESTAMP NOT NULL,
    fim TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AGENDADO',
    tipo VARCHAR(30) NOT NULL DEFAULT 'TREINO',
    observacoes TEXT,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_agendamento_personal FOREIGN KEY (personal_id) REFERENCES usuarios_personais(id),
    CONSTRAINT fk_agendamento_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id),
    CONSTRAINT ck_agendamento_horario CHECK (fim > inicio),
    CONSTRAINT ck_agendamento_status CHECK (status IN ('AGENDADO','CONFIRMADO','REALIZADO','CANCELADO','FALTOU')),
    CONSTRAINT ck_agendamento_tipo CHECK (tipo IN ('TREINO','AVALIACAO','CONSULTA','OUTRO'))
);

CREATE INDEX IF NOT EXISTS idx_agendamento_personal_inicio ON agendamentos(personal_id, inicio);
CREATE INDEX IF NOT EXISTS idx_agendamento_aluno_inicio ON agendamentos(aluno_id, inicio);
