# Etapa 148 — Sincronização de alertas por eventos financeiros

## Objetivo
Atualizar os alertas persistentes de metas depois de alterações financeiras, sem depender exclusivamente de abrir as telas.

## Implementação
- Criado `SincronizarMetaReceitaEvent`, com identificador do personal e período afetado.
- Criado listener com `@TransactionalEventListener(AFTER_COMMIT)`, para executar após a confirmação da transação.
- O registro e a remoção de vínculo entre ação de CRM e recebimento publicam evento para o mês do pagamento.
- A atualização de uma conta como paga publica eventos para os meses dos pagamentos anteriores e para o novo mês, cobrindo alteração de data e substituição dos pagamentos.
- Salvar uma meta mensal publica evento para o período atualizado.
- Não adiciona tabelas nem exige migração de banco.

## Testes recomendados
1. Vincular um pagamento a uma ação e conferir a notificação do mês do pagamento.
2. Remover o vínculo e verificar que a condição do alerta é atualizada após o commit.
3. Alterar a data/parcelamento de pagamento e conferir os meses antigo e novo.
4. Salvar uma meta nova e conferir que o alerta correspondente é atualizado.
5. Confirmar que falha/rollback não dispara a sincronização após commit.

## Limitações
A sincronização cobre os fluxos de aplicação alterados nesta etapa. Alterações feitas diretamente no banco ou por outros processos externos não publicam esses eventos. A compilação e os testes integrados devem ser executados no ambiente do projeto.
