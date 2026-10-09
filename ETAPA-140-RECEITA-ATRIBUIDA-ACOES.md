# Etapa 140 — Receita atribuída às ações de CRM

## Objetivo
Consolidar pagamentos reais vinculados às ações de CRM e apresentar a receita atribuída por tipo de ação.

## Implementação
- Nova rota: `/financeiro/receita-atribuida-acoes`.
- O relatório respeita o personal autenticado, consultando os vínculos associados ao seu cadastro.
- O filtro mensal utiliza a data efetiva do pagamento.
- Indicadores: receita atribuída, quantidade de pagamentos vinculados e quantidade de ações distintas com receita.
- Agrupamento por tipo de ação: receita atribuída, pagamentos, ações distintas e valor médio por pagamento.
- Histórico dos pagamentos considerados e acesso à tela de gerenciamento dos vínculos.
- A tela do Financeiro recebe um atalho para o novo relatório.

## Critérios e limites
- Só entram no relatório pagamentos vinculados a uma ação; pagamentos sem vínculo ficam fora.
- O serviço da Etapa 139 impede atribuir o mesmo pagamento a mais de uma ação.
- Receita atribuída não equivale a lucro líquido nem comprova causalidade por si só.
- A consulta usa os vínculos do personal e o filtro da data do pagamento.

## Validação
O pacote deve passar por compilação Maven e teste manual com dados reais de homologação antes da publicação. A integridade do arquivo ZIP foi verificada; a compilação Maven não é declarada como validada neste ambiente.
