# Etapa 175 — Exportação CSV do dashboard de importações

## Entregas
- Dashboard de importações com filtro por data inicial e final (padrão: últimos 30 dias).
- Indicadores agregados: importações, linhas analisadas, importados, ignorados, rejeitados e percentual de problemas.
- Exportação CSV compatível com Excel (UTF-8 com BOM e separador `;`) contendo resumo do período e detalhamento por importação.
- A consulta é limitada ao personal autenticado, inclusive no endpoint de exportação.
- Links de acesso ao dashboard nas telas de importação e histórico.

## Rotas
- `GET /alunos/importacao/dashboard`
- `GET /alunos/importacao/dashboard.csv`

Ambas aceitam `dataInicio` e `dataFim` no formato `yyyy-MM-dd`; se omitidos, usam os últimos 30 dias.
