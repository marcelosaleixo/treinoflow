CREATE TABLE IF NOT EXISTS lembretes_agendamento (
    id BIGSERIAL PRIMARY KEY,
    agendamento_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    data_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    canal VARCHAR(30) NOT NULL DEFAULT 'WHATSAPP',
    CONSTRAINT fk_lembrete_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos(id) ON DELETE CASCADE,
    CONSTRAINT uk_lembrete_agendamento_tipo UNIQUE (agendamento_id, tipo)
);

CREATE INDEX IF NOT EXISTS idx_lembrete_agendamento_data ON lembretes_agendamento(agendamento_id, tipo);
