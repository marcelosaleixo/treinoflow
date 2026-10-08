# Correção — Cabeçalho azul no Perfil Inteligente

## Causa
A página `aluno/perfil-inteligente.html` carregava `treinoflow.css`, que usa `.bar` para o cabeçalho principal. O CSS local da página também usava `.bar` para as barras de progresso do índice de aderência, sobrescrevendo o cabeçalho e deixando-o com aparência incorreta.

## Correção
A classe visual da barra de progresso foi renomeada de `.bar` para `.score-progress`, incluindo o elemento filho `.score-progress span`. O cabeçalho continua utilizando exclusivamente a classe global `.bar`.

## Resultado
O cabeçalho do Perfil Inteligente volta a utilizar a cor azul padrão `#172554`, igual à tela de Alunos e demais telas padronizadas.
