# Etapa 130 — Metas financeiras

## Entrega
- Cadastro de meta mensal de receita e limite de despesas por Personal.
- Indicadores de receita efetivamente recebida, despesas pagas, resultado e percentual de progresso.
- Filtro por ano/mês e observação.
- Isolamento por `personal_id` e unicidade de uma meta por mês/Personal.

## Dados
A entidade `MetaFinanceira` usa a criação/atualização de schema já configurada no projeto. Não foi incluído script SQL manual.

## Regras
- Valores não podem ser negativos.
- O percentual é calculado com base em receitas pagas no período; sem meta de receita, o progresso fica em 0%.
- O limite de despesas igual a zero é tratado como não definido para fins de alerta.
- Indicadores são gerenciais e não substituem contabilidade.

## Rota
`GET/POST /financeiro/metas`

## Validação
O ZIP foi verificado estruturalmente. A compilação Maven deve ser executada no ambiente do projeto antes do deploy.
