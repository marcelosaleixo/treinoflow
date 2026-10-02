# TreinoFlow — Etapa 9: Acompanhamento da evolução física

## Implementado
- Cadastro, listagem, edição e exclusão de avaliações físicas por aluno.
- Registro da data, peso, altura, percentual de gordura e medidas de cintura, quadril, tórax, braço e coxa, além de observações.
- Isolamento: cada operação valida que o aluno pertence ao personal autenticado.
- Injeção de dependências por atributo com `@Autowired`, conforme padrão solicitado.
- Telas responsivas para listagem e formulário; acesso às avaliações pela lista de alunos.
- Validação de valores não negativos e percentual de gordura entre 0 e 100.

## Banco de dados
A entidade `AvaliacaoFisica` cria a tabela `avaliacoes_fisicas` com relacionamento obrigatório para `alunos`. Em desenvolvimento, `ddl-auto=update` pode atualizar o esquema. Para produção, recomenda-se migração versionada (Flyway/Liquibase) e backup antes de aplicar alterações.

## Rotas
- `GET /alunos/{alunoId}/avaliacoes`
- `GET /alunos/{alunoId}/avaliacoes/nova`
- `GET /alunos/{alunoId}/avaliacoes/{id}/editar`
- `POST /alunos/{alunoId}/avaliacoes/salvar`
- `POST /alunos/{alunoId}/avaliacoes/{id}/excluir`

## Observação
A compilação e o teste integrado com PostgreSQL precisam ser executados no ambiente local. As medidas são registros informados pelo profissional; o sistema não calcula diagnóstico ou prescrição clínica.
