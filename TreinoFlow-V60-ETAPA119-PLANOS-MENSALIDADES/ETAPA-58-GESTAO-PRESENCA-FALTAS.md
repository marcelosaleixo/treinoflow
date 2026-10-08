# ETAPA 58 — Gestão de Presença e Faltas

## Objetivo
Transformar a agenda do TreinoFlow em uma fonte de indicadores de comparecimento e risco de abandono.

## Entregas
- Dashboard `/agenda/presenca`.
- Indicadores dos últimos 30 dias: agendamentos não cancelados, realizados, faltas, confirmados e pendências de fechamento.
- Taxa de comparecimento baseada em `REALIZADO / (REALIZADO + FALTOU)`.
- Visão individual por aluno ativo.
- Situações `RISCO`, `ATENÇÃO`, `NORMAL` e `EXCELENTE`.
- Aluno em risco quando possui 2 ou mais faltas, ou pelo menos 3 sessões encerradas e taxa inferior a 75%.
- Link para Plano de Ação e CRM.
- Histórico dos últimos agendamentos.
- Botão `Faltou` na agenda para horários já iniciados.
- Regra de domínio impede registrar `REALIZADO` ou `FALTOU` antes do início do agendamento.

## Banco de dados
Não foi criada nova tabela. A etapa reutiliza `agendamentos.status`, já existente desde a Etapa 56.

## Próximo passo
A presença poderá alimentar automaticamente o motor de retenção, gerando tarefas e mensagens para alunos com faltas recorrentes.
