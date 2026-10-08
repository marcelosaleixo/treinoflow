# Correção — LazyInitializationException no Financeiro

## Problema
As telas `financeiro/index` e `financeiro/mensalidades` acessavam `c.aluno.nome` / `p.aluno.nome` depois que a sessão Hibernate já havia sido encerrada.

## Causa raiz
`ContaReceber.aluno` e `PlanoMensalidade.aluno` são relacionamentos `LAZY`. As consultas que alimentavam as telas retornavam as entidades sem inicializar `aluno`.

## Correção
As consultas de listagem usadas pelas telas agora usam `@EntityGraph(attributePaths = "aluno")`, carregando somente o relacionamento necessário para essas listagens.

Não foi alterado para `FetchType.EAGER`, não houve alteração de banco e não houve alteração da regra de negócio.

## Consultas corrigidas
- `PlanoMensalidadeRepository.findByPersonalIdOrderByAtivoDescAlunoNomeAsc`
- `ContaReceberRepository.findTop12ByPersonalIdOrderByDataVencimentoAsc`
- `ContaReceberRepository.findByPersonalIdOrderByDataVencimentoAsc`
