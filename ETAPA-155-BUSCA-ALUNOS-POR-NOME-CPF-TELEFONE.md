# Etapa 155 — Pesquisa de alunos por nome, CPF e telefone

## Alterações
- A busca da lista de alunos aceita nome, e-mail, telefone e CPF.
- Para CPF, a aplicação remove pontuação e espaços antes de pesquisar, compatível com CPF armazenado apenas com dígitos.
- A pesquisa continua paginada e sempre limitada ao `personalId` autenticado.
- O placeholder do campo de busca foi atualizado.

## Validação
Execute `./mvnw clean compile` ou `\.\mvnw.cmd clean compile` no Windows e teste buscas por nome, telefone e CPF completo/parcial. A compilação não foi executada neste ambiente.
