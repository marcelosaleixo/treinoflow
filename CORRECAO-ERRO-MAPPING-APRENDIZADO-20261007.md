# Correção — Ambiguous Mapping no painel de Aprendizado

## Erro
O Spring Boot não iniciava porque dois controllers registravam o mesmo endpoint:

`GET /assistente/aprendizado`

- `AprendizadoAcoesAssistenteController`
- `AprendizadoRetencaoController`

## Causa raiz
A Etapa 80 adicionou um novo controller para o painel de aprendizado sem remover o controller legado da Etapa 71/anteriores. O Spring MVC não permite dois métodos de handler com o mesmo método HTTP e URL.

## Correção
O controller legado `AprendizadoAcoesAssistenteController` foi removido. O endpoint oficial passa a ser responsabilidade de `AprendizadoRetencaoController`, que alimenta o painel da Etapa 80.

O serviço `AprendizadoAcoesAssistenteService` foi preservado para não quebrar outras referências existentes.

## Endpoint final
`GET /assistente/aprendizado`

## Resultado esperado
O contexto Spring deve inicializar sem `Ambiguous mapping` e o painel de Aprendizado Contínuo deve continuar acessível em `/assistente/aprendizado`.
