# Correção do campo Objetivo do aluno

- Criado `enums/ObjetivoAluno.java` com as opções padronizadas e seus rótulos de apresentação.
- O controller monta a lista de opções a partir do enum, evitando strings duplicadas no código.
- Corrigida a expressão Thymeleaf do valor legado/customizado: ela acessa `aluno.objetivo` pelo contexto `${...}`, e não como seleção relativa `*{...}`.
- A propriedade persistida `Aluno.objetivo` continua como `String` para preservar os valores existentes no PostgreSQL e evitar migração de coluna.

Recompile e reinicie a aplicação. Nenhum script SQL é necessário.
