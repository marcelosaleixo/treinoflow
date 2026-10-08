# Correção — Feedback sobreposto aos exercícios no celular

## Problema
A caixa **“Seu feedback ajuda o Personal a entender seu treino”** estava com `position: sticky`, fazendo o formulário de feedback permanecer fixo sobre o conteúdo durante a rolagem. Em telas pequenas isso cobria os exercícios e impedia o aluno de visualizar/usar o treino corretamente.

## Correção
O formulário `.finish` voltou ao fluxo normal da página:

- `position: static`;
- `z-index: auto`;
- espaçamento superior após os exercícios;
- no mobile, reforçado o fluxo normal e reduzida a sombra.

O feedback continua sendo exibido depois dos exercícios, mas não fica mais sobreposto à execução do treino.

## Escopo
- Sem alteração de banco de dados.
- Sem alteração de controller.
- Sem alteração de endpoints.
- Sem alteração de autenticação.
- Apenas correção de layout/responsividade em `portal/treino.html`.

## Validação
Arquivo HTML/CSS revisado e ZIP validado estruturalmente.
