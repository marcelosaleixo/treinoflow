# Etapa 75 — Automação Inteligente de Retenção

## Objetivo
Transformar as recomendações da Etapa 74 em execução automática opcional, com controles de segurança e auditoria.

## Regras
- Automação desativada por padrão.
- WhatsApp automático: somente risco crítico (score >= 75), telefone válido e recomendação adaptativa = WhatsApp.
- Follow-up automático: somente risco alto (score >= 50) e recomendação adaptativa = Follow-up.
- Não executa se houver follow-up pendente.
- Não executa se houve ação automática para o aluno dentro do cooldown.
- Respeita horário permitido e limite diário.
- Cada ação automática é marcada em `acoes_assistente` com regra de automação.
- WhatsApp também gera interação de retenção no CRM.

## Tela
`/assistente/automacao`

Permite ativar/desativar a automação, configurar regras, score mínimo, limite diário, cooldown e horário. Há também execução manual controlada para testes.

## Banco
`V46__automacao_retencao_inteligente.sql`
- `automacoes_retencao_config`
- `acoes_assistente.automatica`
- `acoes_assistente.regra_automacao`

## Scheduler
Executa de hora em hora e só atua dentro do horário configurado. O padrão é 08:00–20:00.
