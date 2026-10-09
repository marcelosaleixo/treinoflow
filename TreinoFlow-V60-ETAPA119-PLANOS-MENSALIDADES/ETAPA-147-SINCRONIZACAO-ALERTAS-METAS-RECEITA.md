# Etapa 147 — Sincronização de alertas de metas de receita

## Objetivo
Manter as notificações persistentes coerentes com a receita vinculada e a meta atual, inclusive quando pagamentos são adicionados, removidos ou alterados.

## Alterações
- O serviço de alertas recalcula a condição do período e atualiza título/mensagem de notificações já existentes.
- Alertas que deixaram de corresponder à situação atual são preservados no histórico e marcados como condição atualizada, em vez de apagados.
- A central `/notificacoes` sincroniza os alertas do mês corrente antes de exibir a lista. A tela `/financeiro/alertas-metas-receita` continua sincronizando o período consultado.
- A chave idempotente existente é preservada; não foi adicionada migração de banco de dados.

## Limitações
A sincronização acontece quando uma das telas relevantes é acessada; esta etapa não adiciona agendamento em segundo plano. Como o schema atual não guarda snapshots de valor/meta por notificação, a mensagem de condição encerrada informa que o alerta foi atualizado e orienta consultar o painel para os valores atuais.

## Validação
O ZIP foi verificado quanto à integridade. A compilação Maven e os testes de execução devem ser confirmados no ambiente de desenvolvimento.
