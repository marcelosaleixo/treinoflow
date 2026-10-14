# Etapa 157 — Ordenação da lista de alunos

- Adicionado seletor de ordenação por nome, data de cadastro e data de início.
- A ordenação usa `Pageable/Sort` do Spring Data e inclui `id` como desempate estável.
- Os filtros de busca, status e objetivo continuam aplicados e limitados ao personal autenticado.
- A consulta paginada remove a ordenação fixa por nome para permitir que `Pageable` controle a ordenação.

## Teste
Execute `./mvnw test` ou `.\mvnw.cmd clean compile` e valide os três modos de ordenação com filtros e paginação.
