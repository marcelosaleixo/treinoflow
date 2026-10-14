# Etapa 176 — Relatório mensal de importações CSV

## Entrega
- Novo relatório mensal agrupado por mês, com intervalo de datas ajustável.
- Indicadores agregados de importações, linhas analisadas, importados, ignorados e rejeitados.
- Percentual mensal de registros ignorados/rejeitados e variação em pontos percentuais contra o mês anterior.
- Exportação CSV compatível com Excel (UTF-8 com BOM e separador `;`).
- Consultas limitadas ao personal autenticado.

## Rotas
- `GET /alunos/importacao/relatorio-mensal`
- `GET /alunos/importacao/relatorio-mensal.csv`

O padrão exibe os seis meses corridos até a data atual. O período pode ser alterado pelos campos de data.
