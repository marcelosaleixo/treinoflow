# ETAPA 49 — Automação de Retenção

## Objetivo
Transformar os alertas do Plano de Ação da Etapa 47 em follow-ups reais no CRM, reduzindo trabalho manual sem enviar mensagens automaticamente.

## Funcionalidades
- Geração automática de follow-ups para alunos identificados pelo Plano de Ação.
- Geração manual pelo botão **Gerar ações agora** em `/retencao`.
- Execução automática diária às 08:15, configurável por `TREINOFLOW_RETENCAO_AUTOMACAO_CRON`.
- Preenchimento automático de canal `WHATSAPP`, tipo `RETENCAO`, resultado `EM_ACOMPANHAMENTO` e próxima ação para o dia atual.
- Descrição do follow-up com motivo, ação recomendada e mensagem sugerida.
- Botão **WhatsApp sugerido** que abre o WhatsApp com a mensagem preparada; o envio continua sendo uma ação do Personal.
- Proteção contra duplicação: não cria nova ação se já existir follow-up pendente para o aluno ou se houve contato nos últimos 7 dias.

## Regras
As ações usam as mesmas regras da Etapa 47:
1. Sem treino concluído nos últimos 30 dias → `RETENCAO`, prioridade ALTA.
2. Sem treino na semana, mas com treino nos últimos 30 dias → `FREQUENCIA`, prioridade ALTA.
3. Menos de 5 sessões nos últimos 30 dias → `ADESAO`, prioridade MEDIA.
4. Média de avaliação abaixo de 3,5 → `SATISFACAO`, prioridade MEDIA.

Apenas a primeira regra aplicável por aluno é transformada em follow-up.

## Segurança
- O personal é resolvido pela autenticação.
- A geração manual usa somente o `personalId` do usuário autenticado.
- O endpoint de WhatsApp valida que a interação pertence ao personal autenticado.
- A automação agendada percorre apenas usuários com perfil `PERSONAL` e ativos.

## WhatsApp
A etapa **não dispara mensagens automaticamente**. O botão abre uma URL `wa.me` com texto pré-preenchido. Isso mantém o controle do contato nas mãos do Personal e evita disparos indevidos.

## Persistência
Não foi criada tabela nova. Os follow-ups são registrados na tabela existente `interacoes_crm`.
