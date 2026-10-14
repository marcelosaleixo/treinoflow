# Etapa 158 — Preservar ordenação na paginação de alunos

## Alterações
- A paginação da lista de alunos agora mantém o critério de ordenação selecionado ao navegar para primeira, anterior, próxima e última página.
- A alteração do tamanho da página mantém os filtros e a ordenação e reinicia na primeira página.
- Os filtros de busca, status e objetivo continuam preservados.
- Alteração aplicada às duas cópias do projeto no pacote.

## Validação manual
1. Acesse `/alunos`.
2. Selecione uma ordenação diferente de nome, por exemplo cadastro ou início.
3. Combine com busca, status e objetivo.
4. Navegue entre páginas e altere o tamanho da página.
5. Confirme que os parâmetros e a ordenação permanecem selecionados.

## Observação
O ZIP foi verificado quanto à integridade. A compilação Maven e a renderização Thymeleaf devem ser testadas no ambiente local.
