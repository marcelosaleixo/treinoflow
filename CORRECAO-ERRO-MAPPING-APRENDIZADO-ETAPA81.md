# Correção — Ambiguous Mapping /assistente/aprendizado

## Causa
Dois controllers registravam GET /assistente/aprendizado:
- AprendizadoRetencaoController (Etapa 80)
- AprendizadoAcoesAssistenteController (Etapa 73)

## Correção
O endpoint oficial permanece em AprendizadoRetencaoController:
`GET /assistente/aprendizado`

O painel legado de aprendizado das ações foi preservado em:
`GET /assistente/aprendizado-acoes?dias=90`

Assim nenhuma funcionalidade antiga é apagada e o Spring possui apenas um handler para o endpoint oficial.

## Limpeza local
Apague `target/` antes de executar para remover classes compiladas de versões anteriores.
