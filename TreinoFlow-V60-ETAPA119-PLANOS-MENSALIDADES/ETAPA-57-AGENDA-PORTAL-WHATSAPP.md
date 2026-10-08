# ETAPA 57 — Agenda + Portal do Aluno + WhatsApp

## Objetivo
Transformar a Agenda do Personal em um fluxo de confirmação e lembrete, reduzindo faltas e conectando o agendamento ao Portal do Aluno.

## Entregas
- Próximos 14 dias de agendamentos exibidos no Portal do Aluno.
- Aluno pode confirmar um agendamento pelo link público do portal.
- Aluno pode cancelar agendamento futuro pelo portal.
- Status `CONFIRMADO` volta imediatamente para a Agenda do Personal.
- Lembrete WhatsApp 24 horas antes.
- Lembrete WhatsApp 2 horas antes.
- Idempotência por agendamento + tipo do lembrete.
- Lembretes só são enviados para `AGENDADO` ou `CONFIRMADO`.
- Alunos sem telefone não geram erro nem envio.
- Nenhuma alteração automática de treino ou execução é realizada.

## Configuração
Para envio real pelo WhatsApp:

```properties
TREINOFLOW_WHATSAPP_ENABLED=true
TREINOFLOW_WHATSAPP_BASE_URL=https://seu-endpoint
TREINOFLOW_WHATSAPP_API_KEY=...
TREINOFLOW_WHATSAPP_INSTANCE=...
TREINOFLOW_APP_BASE_URL=https://seu-dominio.com
```

O job padrão executa a cada 15 minutos. A janela de tolerância evita perda do lembrete se o job não executar exatamente no minuto do aniversário de 24h/2h.

## Banco
Migration: `V44__lembretes_agendamento.sql`.

Tabela: `lembretes_agendamento`.

A chave única `(agendamento_id, tipo)` impede duplicidade de lembretes.
