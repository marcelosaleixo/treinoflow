# ETAPA 120 — Histórico Financeiro + Filtros

## Objetivo
Criar uma visão completa das contas a receber do Personal, aproveitando a estrutura financeira das Etapas 118 e 119.

## Entregas
- Histórico completo em `/financeiro/contas-receber`.
- Filtros por status, aluno, forma de pagamento e intervalo de vencimento.
- Resumo do total filtrado e total já recebido.
- Ações de receber e cancelar preservadas com validação de ownership.
- Atalho no dashboard financeiro.
- Layout responsivo.

## Banco
Não foi criada nova tabela nem nova migration. A etapa reutiliza `contas_receber_personal` e `planos_mensalidade_personal`.

## Segurança
Todas as consultas continuam limitadas ao `personal.id` autenticado. O aluno selecionado também é validado antes da consulta.

## Próxima etapa sugerida
ETAPA 121 — Despesas e categorias, para permitir calcular resultado/lucro do Personal.
