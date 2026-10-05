# ETAPA 41 — Automação Comercial e Retenção

## Objetivo
Transformar as notificações financeiras da Etapa 40 em uma rotina de retenção automática, com lembretes progressivos para cobranças em atraso.

## Regras implementadas

1. A rotina diária continua atualizando cobranças vencidas.
2. Ao existir cobrança atrasada, o TreinoFlow gera a notificação inicial `COBRANCA_ATRASADA`.
3. No 3º dia de atraso, gera `INADIMPLENCIA_3_DIAS`.
4. No 7º dia de atraso, gera `INADIMPLENCIA_7_DIAS`.
5. A geração é idempotente: a mesma cobrança/data/evento não gera duplicidade.
6. Cobranças pendentes com vencimento em 7 dias também podem gerar `ASSINATURA_VENCENDO`.
7. Os canais continuam sendo controlados pela configuração da Etapa 39: interno, e-mail e WhatsApp.
8. Os textos de e-mail e WhatsApp são definidos pela Central de Comunicação da Etapa 40.

## Migration

`V39__automacao_retencao.sql`

Inclui os templates padrão para os novos eventos:
- `INADIMPLENCIA_3_DIAS`
- `INADIMPLENCIA_7_DIAS`

## Fluxo comercial

```text
Cobrança criada
      ↓
7 dias antes ──→ lembrete de vencimento
      ↓
Data do vencimento ──→ lembrete de vencimento hoje
      ↓
Atraso ──→ cobrança atrasada
      ↓
3 dias ──→ aviso de recuperação
      ↓
7 dias ──→ último aviso
```

## Idempotência
A chave utilizada continua sendo baseada em cobrança + tipo + referência. Assim, a rotina pode executar diariamente sem criar notificações duplicadas.

## Configuração
Nenhuma credencial nova é necessária. E-mail e WhatsApp continuam dependentes das propriedades da Etapa 39.
