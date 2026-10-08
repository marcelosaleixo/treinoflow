CREATE TABLE IF NOT EXISTS eventos_compartilhamento (
    id BIGSERIAL PRIMARY KEY,
    treino_id BIGINT NOT NULL REFERENCES treinos(id),
    tipo_evento VARCHAR(20) NOT NULL,
    data_evento TIMESTAMP NOT NULL,
    detalhe VARCHAR(250)
);
CREATE INDEX IF NOT EXISTS idx_evento_treino_data ON eventos_compartilhamento (treino_id, data_evento);
