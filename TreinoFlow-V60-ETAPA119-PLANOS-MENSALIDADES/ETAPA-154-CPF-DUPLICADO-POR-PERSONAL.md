# Etapa 154 — CPF duplicado por personal

## Alterações
- Impede cadastrar dois alunos com o mesmo CPF na lista do mesmo personal.
- Permite que profissionais diferentes tenham alunos com o mesmo CPF, respeitando o isolamento dos dados por personal.
- Na edição, a verificação ignora o próprio registro do aluno.
- CPF continua opcional; quando informado, a validação de dígitos verificadores existente permanece ativa.
- Erro de duplicidade retorna mensagem de validação clara.

## Persistência
Foram adicionados métodos derivados no `AlunoRepository`. Não foi criada restrição única global no banco, para não impedir o mesmo aluno de existir sob profissionais diferentes nem causar falha de migração por dados históricos. Se a aplicação permitir gravações concorrentes, considere futuramente uma restrição única composta por `(personal_id, cpf)` após auditar duplicidades existentes.

## Teste
1. Cadastrar um aluno com CPF válido.
2. Tentar cadastrar outro aluno do mesmo personal com o mesmo CPF: deve ser recusado.
3. Editar o aluno mantendo o próprio CPF: deve ser permitido.
4. Editar para CPF já utilizado por outro aluno do mesmo personal: deve ser recusado.
5. Cadastrar o mesmo CPF para outro personal: deve ser permitido.

Executar `./mvnw test` (Linux/macOS) ou ` .\mvnw.cmd test` (Windows).
