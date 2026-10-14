# Etapa 160 — Exportação Excel de Alunos

## Implementação
- Endpoint `GET /alunos/exportar.xlsx`.
- Botão **Exportar Excel** na lista de alunos.
- Planilha `.xlsx` com cabeçalho em negrito, filtro automático, primeira linha congelada e largura ajustada das colunas.
- Exporta os mesmos campos e respeita busca, status, objetivo e ordenação.
- Consulta limitada ao personal autenticado.
- Dependência Apache POI `poi-ooxml` para gerar a planilha.

## Testes recomendados
1. Executar `./mvnw clean compile` (Windows: `\.\mvnw.cmd clean compile`).
2. Acessar `/alunos`, aplicar filtros e exportar.
3. Abrir o arquivo no Excel ou LibreOffice.
4. Confirmar filtros, ordenação e isolamento por personal.
