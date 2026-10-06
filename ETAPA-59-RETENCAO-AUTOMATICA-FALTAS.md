# ETAPA 59 — Retenção Automática por Faltas

## Objetivo
Transformar os dados de presença da Etapa 58 em uma fila operacional de retenção, usando o CRM existente e sem criar uma nova tabela.

## Regras
- Analisa os últimos 30 dias.
- Ignora agendamentos cancelados.
- Risco quando o aluno tem 2 ou mais faltas.
- Também é risco quando existem pelo menos 3 sessões encerradas e a taxa de comparecimento é inferior a 75%.
- Prioridade ALTA quando há 3+ faltas ou taxa inferior a 60% com pelo menos 4 sessões encerradas; caso contrário, MEDIA.

## Ação automática
- Cria `InteracaoCrm` com tipo `RETENCAO`.
- Resultado inicial `EM_ACOMPANHAMENTO`.
- Define próxima ação para hoje.
- Usa WhatsApp como canal quando há telefone; caso contrário usa canal interno.
- Não cria duplicidade se já existir follow-up pendente ou contato nos últimos 7 dias.
- A rotina diária existente de retenção também executa esta análise.

## Interface
- Nova rota: `/retencao/faltas`.
- Lista de alunos em risco por faltas.
- Prioridade, taxa de comparecimento, faltas, realizados e último agendamento.
- Atalhos para CRM, WhatsApp, Presença e Ciclo de Retenção.
- Botão para criar ações imediatamente.

## Banco
Nenhuma nova tabela ou migration foi necessária. A etapa reutiliza `agendamentos` e `interacoes_crm`.
