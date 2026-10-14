# Etapa 156 — Filtros de alunos por status e objetivo

- Adicionados filtros combináveis de status (ativo/inativo) e objetivo à lista de alunos.
- Mantida a busca por nome, CPF, e-mail e telefone.
- Consultas continuam limitadas ao personal autenticado.
- Paginação preserva termo de busca e os filtros selecionados.
- O filtro de status aceita apenas `ATIVO` e `INATIVO`; valores inesperados são rejeitados no serviço.

## Teste
Execute `./mvnw clean compile` (Windows: `\.\mvnw.cmd clean compile`) e teste combinações de busca, status, objetivo e paginação.
