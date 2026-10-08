# Etapa 29 — Histórico de compartilhamento

- Registra eventos de geração/liberação e revogação de links públicos.
- Exibe data/hora e detalhe do evento no editor do treino, em ordem decrescente.
- A consulta valida a propriedade do treino pelo personal autenticado.
- Requer tabela `eventos_compartilhamento`; com `ddl-auto=update` ela será criada automaticamente. Em produção com `validate`, aplicar a migração SQL abaixo.

```sql
CREATE TABLE IF NOT EXISTS eventos_compartilhamento (
    id BIGSERIAL PRIMARY KEY,
    treino_id BIGINT NOT NULL REFERENCES treinos(id),
    tipo_evento VARCHAR(20) NOT NULL,
    data_evento TIMESTAMP NOT NULL,
    detalhe VARCHAR(250)
);
CREATE INDEX IF NOT EXISTS idx_evento_treino_data
    ON eventos_compartilhamento (treino_id, data_evento);
```

- Treinos com eventos registrados não podem ser excluídos, para preservar o histórico de auditoria.
