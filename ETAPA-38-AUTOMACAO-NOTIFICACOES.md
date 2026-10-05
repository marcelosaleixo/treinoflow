# ETAPA 38 — Automação financeira e notificações

## Objetivo
Automatizar avisos financeiros sem depender de intervenção manual do Master.

## Entregas
- Central de notificações do Personal.
- Aviso 7 dias antes do vencimento.
- Aviso no dia do vencimento.
- Aviso de cobrança atrasada.
- Confirmação de pagamento.
- Agendamento diário configurável.
- Idempotência por cobrança + tipo + referência, evitando duplicidade.
- Dashboard com contador de não lidas e últimas notificações.
- Migration V36.

## Segurança
As notificações são sempre filtradas pelo `personal_id` autenticado. O endpoint de marcar como lida valida a propriedade da notificação antes de alterar o registro.

## Execução automática
Por padrão, a rotina roda às 08:00:

`treinoflow.notificacoes.cron=0 0 8 * * *`

Pode ser sobrescrita por `TREINOFLOW_NOTIFICACOES_CRON`.

## Canais
Esta etapa implementa o canal interno do sistema. A arquitetura deixa a entrega externa (e-mail/WhatsApp) para uma etapa posterior, evitando acoplar o núcleo financeiro a um provedor sem configuração de produção.
