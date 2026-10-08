# Correção — Follow-up Inteligente

## Problema
A tela `/performance/follow-up-inteligente` estava visualmente desconfigurada. O fragmento de cabeçalho era carregado sem o `tf-header.css` nesta página e o grid do conteúdo permitia que textos muito longos expandissem uma coluna, comprimindo a coluna de próxima ação.

## Correções
- Inclusão do `tf-header.css` no template.
- Reset visual básico e tipografia consistente com o TreinoFlow.
- Grid com `minmax(0, ...)` para impedir expansão horizontal causada por conteúdo longo.
- `min-width:0` nos blocos do grid.
- Quebra segura de textos longos com `overflow-wrap:anywhere`.
- Melhor comportamento responsivo em desktop e mobile.
- Nenhuma alteração de regra de negócio, banco de dados ou endpoint.
