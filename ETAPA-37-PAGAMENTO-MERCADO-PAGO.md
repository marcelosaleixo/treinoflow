# Etapa 37 — Pagamento online com PIX / Mercado Pago

## Objetivo
Transformar a cobrança manual da Etapa 36 em um fluxo de pagamento online sem misturar regras financeiras com detalhes do provedor.

## Implementado
- Interface `PagamentoGateway` para desacoplar o domínio do provedor.
- Implementação `MercadoPagoGateway` usando a API HTTPS do Mercado Pago.
- Criação de order PIX com `X-Idempotency-Key` determinístico por cobrança.
- Persistência do ID externo, referência, QR Code, QR Code Base64 e URL de pagamento.
- Tela do Personal para abrir/visualizar o PIX.
- Webhook `POST /webhooks/mercadopago/orders`.
- Validação HMAC-SHA256 do `x-signature` usando `x-request-id` e `data.id`.
- Consulta da order no Mercado Pago antes de marcar a cobrança como paga.
- Ao confirmar pagamento, a cobrança vira `PAGA` e a assinatura é reativada quando aplicável.
- Migration V35 para os dados do gateway.

## Configuração
Defina no ambiente:

```text
TREINOFLOW_MP_ACCESS_TOKEN=APP_USR-...
TREINOFLOW_MP_NOTIFICATION_URL=https://SEU-DOMINIO/webhooks/mercadopago/orders
TREINOFLOW_MP_WEBHOOK_SECRET=...
```

O Access Token permanece somente no backend. A URL de notificação deve ser pública e HTTPS. Configure o webhook no Mercado Pago para o evento de Orders e utilize as credenciais de teste antes da produção.

## Fluxo

```text
Personal
  -> Minhas cobranças
  -> Pagar / PIX
  -> TreinoFlow cria Order PIX
  -> QR Code / código PIX
  -> Mercado Pago envia Webhook
  -> TreinoFlow valida assinatura
  -> Consulta Order na API
  -> cobrança PAGA
  -> assinatura ATIVA
  -> próxima cobrança calculada
```

## Segurança
Nunca exponha o Access Token no frontend ou no Git. O webhook é público somente no sentido HTTP e exige validação da assinatura secreta antes de processar a notificação.

## Limitações desta etapa
- Não implementa cartão nem assinatura recorrente automática do gateway.
- Não simula pagamento localmente.
- O funcionamento real depende de credenciais Mercado Pago e URL HTTPS pública configuradas.
