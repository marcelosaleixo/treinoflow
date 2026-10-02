# TreinoFlow — Etapa 22: Filtro de status na listagem de treinos

## Implementado
- Filtro por Todos, Rascunho e Liberado na lista de treinos do aluno.
- Combina filtro de status com busca pelo nome.
- Mantém busca e status ao navegar entre páginas e alterar quantidade por página.
- Valida os valores de status recebidos e preserva o escopo do aluno pertencente ao personal autenticado.
- Atualiza o fragmento de paginação para transportar o status, com valor vazio nas demais listagens.

## Teste manual
1. Abra a lista de treinos de um aluno.
2. Selecione Rascunho e depois Liberado; confirme os registros exibidos.
3. Combine o status com a busca pelo nome.
4. Navegue entre páginas e altere o tamanho da página; os filtros devem permanecer.
5. Selecione Todos e confirme a listagem completa.

## Validação
A compilação não foi concluída neste ambiente porque o Maven Wrapper não conseguiu baixar o Maven de `repo.maven.apache.org`. Execute `mvnw.cmd clean test` no Windows ou `./mvnw clean test` em ambiente com acesso ao repositório Maven.
