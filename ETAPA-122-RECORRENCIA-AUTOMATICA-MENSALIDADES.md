# ETAPA 122 — Recorrência automática de mensalidades

## Objetivo
Transformar os planos mensais cadastrados pelo Personal em cobranças recorrentes sem exigir que o Personal entre no sistema todos os meses para gerar manualmente a conta a receber.

## Funcionamento
- Todo plano mensal ativo é verificado diariamente.
- A cobrança do mês é criada quando o vencimento chega.
- Se a aplicação estiver indisponível no dia do vencimento, o próximo processamento recupera a cobrança do mês e a marca como `ATRASADA` quando o vencimento já passou.
- Antes de criar, o sistema verifica `plano_mensalidade_id + data_vencimento`, evitando a duplicação no fluxo normal da aplicação.
- Planos pausados não geram novas cobranças.
- Planos cuja data de início ainda não chegou não geram cobranças.
- A cobrança continua sendo uma `ContaReceber` vinculada ao plano e ao aluno.

## Segurança e isolamento
O processamento manual disponibilizado na tela usa o Personal autenticado e processa somente os planos pertencentes a ele.

## Processamento manual
A tela `/financeiro/mensalidades` ganhou o botão **Processar agora**, útil para teste e para recuperação imediata sem esperar o agendamento.

## Agendamento
Propriedade:

`treinoflow.financeiro.recorrencia-cron`

Padrão:

`0 0 7 * * *`

Pode ser sobrescrita por:

`TREINOFLOW_FINANCEIRO_RECORRENCIA_CRON`

## Escopo preservado
- Não cria cobrança em gateway.
- Não envia WhatsApp ou e-mail.
- Não altera pagamentos já registrados.
- Não altera o valor do plano.
- Não altera a regra de pagamentos divididos da Etapa 121.
- Não exige nova tabela.

## Próxima evolução natural
A próxima etapa pode usar as contas recorrentes para implementar **cobrança inteligente**, com lembrete antes do vencimento, cobrança após atraso, histórico de tentativas e, posteriormente, integração com PIX/Mercado Pago.
