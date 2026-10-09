# Correção visual — botão Previsão financeira

## Problema
O botão “Previsão financeira” aparecia acima do cabeçalho/menu principal na tela Financeiro.

## Correção
O botão foi removido da faixa separada acima do menu e inserido junto aos demais atalhos dentro da área de ações do Financeiro. Assim, mantém o mesmo alinhamento e comportamento responsivo dos outros botões.

## Arquivos
- `src/main/resources/templates/financeiro/index.html`
- Cópia do projeto aninhada `TreinoFlow-V60-ETAPA119-PLANOS-MENSALIDADES/src/main/resources/templates/financeiro/index.html`

## Validação
Foi conferido que o botão não permanece antes do fragmento do cabeçalho e aparece na área `.actions`. O ZIP deve ser testado com compilação e execução no ambiente local; a compilação Maven não foi executada nesta correção.
