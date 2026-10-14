# Etapa 159 — Exportação CSV da lista de alunos

- Adicionada rota `GET /alunos/exportar.csv`.
- Exporta os resultados conforme busca, status, objetivo e ordenação selecionados na tela.
- Inclui nome, CPF, sexo, e-mail, telefone, objetivo, status e datas cadastrais.
- A consulta é limitada ao personal autenticado e percorre as páginas do resultado.
- CSV em UTF-8 com BOM e separador `;`, adequado para abrir em planilhas comuns no Brasil.
- Valores são escapados e prefixos que podem ser interpretados como fórmulas são neutralizados.

## Testar
Acesse `/alunos`, aplique filtros e clique em **Exportar CSV**. Confira se o arquivo contém somente os alunos do personal e os filtros ativos.

## Validação
Executar `./mvnw clean compile` ou `\.\mvnw.cmd clean compile` no Windows e testar com dados reais antes de publicar.
