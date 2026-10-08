# Correção — LazyInitializationException em Planos e Mensalidades

## Problema
A tela `financeiro/mensalidades.html` acessava `p.aluno.nome`. O relacionamento `PlanoMensalidade.aluno` estava configurado como `LAZY`, e o carregamento acontecia depois do encerramento da transação do service. Isso provocava `LazyInitializationException` ao renderizar o Thymeleaf.

## Causa raiz
A consulta `findByPersonalIdOrderByAtivoDescAlunoNomeAsc` retornava `PlanoMensalidade` com o proxy de `Aluno` não inicializado. O Thymeleaf tentava executar `p.aluno.nome` fora da sessão Hibernate.

## Correção aplicada
Foi adicionado `@EntityGraph(attributePaths = "aluno")` no método de listagem do `PlanoMensalidadeRepository`.

Isso instrui o Spring Data JPA a carregar o `Aluno` junto com cada `PlanoMensalidade` nessa consulta específica, sem transformar o relacionamento global em `EAGER`.

## Por que esta solução é a adequada
- Mantém `@ManyToOne(fetch = FetchType.LAZY)`.
- Evita Open Session in View como solução para o problema.
- Evita N+1 desnecessário nessa listagem.
- Não altera banco de dados.
- Não altera regras de negócio.
- Corrige diretamente a causa do erro de renderização.

## Erro original
`LazyInitializationException: Could not initialize proxy [com.marceloaleixo.treinoflow.entity.Aluno#2] - no session`

## Validação
O projeto foi inspecionado após a alteração. A compilação Maven não pôde ser concluída neste ambiente porque o Maven Wrapper tentou baixar o Maven 3.9.16 do Maven Central e o download falhou por indisponibilidade de rede.
