# ETAPA 90 — Feedback do Personal sobre a IA

## Objetivo

Transformar as decisões do Personal em sinais explícitos de aprendizado do TreinoFlow, sem alterar automaticamente o treino com base no feedback.

## Fluxo

1. A IA sugere uma progressão.
2. O Personal aprova a aplicação.
3. A Etapa 89 registra a prescrição anterior e posterior.
4. A Etapa 90 permite avaliar a recomendação:
   - `ACEITA` — recomendação aprovada sem ajuste informado.
   - `ACEITA_AJUSTADA` — recomendação útil, mas o Personal fez ajustes.
   - `NAO_CONCORDEI` — recomendação não foi considerada adequada.
5. Para ajuste ou discordância, o sistema solicita um motivo.
6. O feedback é salvo na mesma auditoria da decisão.

## Segurança

- O feedback só pode ser registrado pelo Personal proprietário do aluno.
- A auditoria precisa pertencer ao aluno informado na URL.
- Apenas decisões `APLICADA` recebem feedback.
- Cada decisão pode receber feedback uma única vez.
- O sistema não reverte nem altera o treino automaticamente após um feedback negativo.

## Persistência

A entidade `PrescricaoAuditoria` recebeu os campos:

- `feedback_decisao`
- `feedback_motivo`
- `feedback_data`

Com `ddl-auto=update`, o Hibernate poderá criar as novas colunas no banco atual.

## Endpoint

`POST /performance/alunos/{alunoId}/evolucao/auditorias/{auditoriaId}/feedback`

Parâmetros:

- `feedback`: `ACEITA`, `ACEITA_AJUSTADA` ou `NAO_CONCORDEI`
- `motivo`: obrigatório nos dois últimos casos.

## Próxima evolução

A Etapa 91 pode consolidar esses sinais em métricas de qualidade da IA e gerar um índice por Personal/exercício, preparando o aprendizado baseado em evidências reais.
