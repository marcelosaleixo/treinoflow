# Correção do erro no editor de treino

## Causa
O controller carregava o treino por `buscarPorId`, que não fazia fetch do aluno. O template acessa `aluno.nome` após o fechamento da sessão do Hibernate. A lista de itens também acessa `item.exercicio.nome`.

## Alterações
- O controller agora chama `buscarPorIdComAluno(treinoId, personalId)`.
- O repository carrega treino + aluno por `JOIN FETCH`, restringindo pela propriedade do personal autenticado.
- A consulta dos itens carrega o exercício associado com `JOIN FETCH`.
- Mantida a injeção por atributo com `@Autowired`.
- Removido atributo de repository duplicado do `TreinoService`.

## Teste
Executar `mvnw.cmd clean test` e iniciar a aplicação. Abrir um treino no editor, verificar exercícios e confirmar que um personal não consegue abrir treino de outro.
