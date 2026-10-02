# Etapa 28 — Revogação auditável de links públicos

- A revogação passa a definir o status `REVOGADO`, em vez de retornar o treino diretamente a `RASCUNHO`.
- O token público é invalidado imediatamente e a data/hora da revogação é registrada.
- O editor informa o estado e o horário da revogação.
- A listagem permite filtrar por `REVOGADO`.
- Ao liberar novamente, o sistema gera um novo token e limpa a data de revogação.
- Migração SQL: `src/main/resources/db/migration/V28__auditoria_revogacao_treino.sql`.

Em bancos com `spring.jpa.hibernate.ddl-auto=validate`, aplique a migração antes de iniciar a aplicação.
