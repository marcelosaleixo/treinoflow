# Etapa 126 — Previsão Financeira

## Objetivo
Apresentar projeção gerencial para 3, 6 ou 12 meses usando dados já registrados no TreinoFlow.

## Implementação
- Nova rota autenticada: `GET /financeiro/previsao`.
- `PrevisaoFinanceiraController`, `PrevisaoFinanceiraService` e `PrevisaoFinanceiraMesView`.
- Nova tela `templates/financeiro/previsao.html`.
- Receita prevista: contas com vencimento no mês que não estejam pagas/canceladas, mais planos ativos sem conta para o mesmo vencimento.
- Despesas previstas: somente despesas pendentes cadastradas para o mês.
- Saldo projetado: receita prevista menos despesas pendentes cadastradas.
- Horizonte limitado a 1–12 meses, com opções de interface para 3, 6 e 12.
- Sem nova tabela ou migração de banco.

## Limitações explícitas
A previsão não garante recebimento e não infere despesas que ainda não foram cadastradas. Não calcula taxa histórica de inadimplência ou crescimento de carteira.

## Segurança
Todas as consultas usam o ID do Personal autenticado.
