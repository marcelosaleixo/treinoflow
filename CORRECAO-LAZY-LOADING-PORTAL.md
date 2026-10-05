# Correção — Portal do Aluno / LazyInitializationException

## Causa

O template `portal/index.html` acessava `aluno.personal.nome`, mas `Aluno.personal` é uma associação `LAZY`. O `PortalAlunoService.buscarAlunoPorToken()` retornava a entidade depois que a sessão/transação de consulta já havia terminado. Quando o Thymeleaf tentou executar `aluno.personal.nome`, o Hibernate tentou inicializar o proxy sem sessão e lançou `LazyInitializationException`.

## Correção

O `AlunoRepository.findByTokenPortal(...)` passou a usar `JOIN FETCH a.personal`, garantindo que o Personal seja carregado junto com o aluno na mesma consulta.

A correção mantém `FetchType.LAZY` na entidade e evita habilitar Open Session in View apenas para resolver o problema da tela.

## Erro original

`Could not initialize proxy [com.marceloaleixo.treinoflow.entity.UsuarioPersonal#2] - no session`

## Resultado esperado

A expressão abaixo no portal passa a funcionar após a consulta:

`${aluno.personal.nome}`
