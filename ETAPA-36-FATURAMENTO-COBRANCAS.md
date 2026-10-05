# Etapa 36 — Faturamento e Cobranças

## Objetivo
Adicionar controle financeiro mensal sem gateway de pagamento.

## Entregas
- Entidade Cobranca.
- Status PENDENTE, PAGA, ATRASADA e CANCELADA.
- Geração idempotente da cobrança do mês.
- Marcação manual como paga.
- Cancelamento de cobrança não paga.
- Atualização da próxima cobrança após pagamento.
- Identificação de cobranças atrasadas e sincronização para INADIMPLENTE.
- Painel Master de cobranças.
- Histórico de cobranças do Personal.
- Migration V34.

## Regra
A geração do mês não duplica cobrança porque existe unicidade por assinatura + competência.

## Fora desta etapa
- Mercado Pago/Stripe.
- PIX automático.
- Webhooks.
- Emissão de nota fiscal.

Esses recursos podem ser adicionados em uma etapa posterior sem substituir a estrutura desta etapa.
