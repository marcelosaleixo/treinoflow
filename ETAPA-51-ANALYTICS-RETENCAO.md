# ETAPA 51 — Analytics de Retenção e Churn

## Objetivo
Transformar os dados já registrados no CRM e na Central de Retenção em indicadores para decisão comercial.

## Entregas
- Nova rota `/retencao/analytics`.
- Indicadores de carteira ativa, alunos em risco, contatos, sucesso e cancelamento nos últimos 30 dias.
- Tempo médio entre primeiro contato registrado e resultado `RECUPERADO`.
- Evolução dos últimos 6 meses.
- Motivos de risco atuais com base no Plano de Ação.
- Lista dos recuperados e renovados recentes.
- Isolamento por `personalId`.
- Layout responsivo para desktop e celular.

## Observação sobre churn
O sistema não possui histórico de snapshots mensais da carteira. Por isso, esta etapa não inventa uma taxa histórica de churn por base ativa. A métrica de cancelamento usa apenas os resultados de CRM registrados no período. Os motivos são tratados como sinais de risco, não como causas comprovadas.
