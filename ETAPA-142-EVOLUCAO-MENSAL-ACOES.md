# Etapa 142 — Evolução mensal da receita atribuída às ações

## Objetivo
Dar ao personal uma visão histórica dos pagamentos efetivamente vinculados às ações de CRM, com comparação do mês selecionado contra o anterior e janela de 12 meses.

## Implementação
- Rota autenticada: `/financeiro/evolucao-acoes`.
- Filtros de ano e mês de referência.
- Histórico de 12 meses: receita vinculada, número de recebimentos e ações distintas com receita.
- Indicadores: receita no mês, variação absoluta e percentual contra o mês anterior, receita acumulada na janela e quantidade de recebimentos.
- Atalho no painel financeiro e links para os relatórios de receita atribuída e conversão.
- Dados consultados a partir dos vínculos de recebimentos do personal autenticado.

## Critérios
- A competência mensal usa a data do pagamento.
- Pagamentos sem data não entram na análise.
- Variação percentual é zero quando a receita do mês anterior é zero, evitando divisão por zero; os valores absolutos permanecem visíveis.
- Receita atribuída não é lucro líquido nem demonstra causalidade por si só.

## Validação
O arquivo ZIP deve ser verificado por integridade. A compilação Maven e os testes com PostgreSQL precisam ser executados no ambiente de desenvolvimento antes da publicação.
