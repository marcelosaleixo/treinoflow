# Correção — Alertas de Metas de Receita (Etapa 145)

## Causa
A variável local `periodo` recebe valores tanto no `try` quanto no `catch`, portanto não é efetivamente final. Ela era referenciada dentro do `filter` de um stream, causando erro de compilação Java e impedindo a inicialização do controlador.

## Correção
Foi criada a variável `final YearMonth periodoSelecionado = periodo` após o tratamento do período inválido, e o `filter` usa essa variável final. Aplicado nos dois exemplares do controlador incluídos no ZIP.

## Validação
O ZIP foi verificado quanto à integridade. A compilação Maven e os testes de execução precisam ser confirmados no ambiente local.
