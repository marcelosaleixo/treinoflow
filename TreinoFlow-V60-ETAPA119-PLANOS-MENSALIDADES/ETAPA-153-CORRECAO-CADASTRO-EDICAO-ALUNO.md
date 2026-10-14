# Etapa 153 — Correção do cadastro e edição de aluno

- Corrigido o campo de nascimento para renderizar explicitamente `yyyy-MM-dd`, formato aceito por `input[type=date]`.
- Adicionados CPF e sexo ao formulário compartilhado de cadastro/edição.
- CPF é opcional; quando informado, remove máscara e valida os dígitos verificadores.
- Sexo opcional com valores `FEMININO`, `MASCULINO` e `NAO_INFORMADO`.
- Incluída a migração PostgreSQL `V49__campos_cpf_sexo_aluno.sql`.

Executar ` .\mvnw.cmd clean compile` e testar cadastro e edição no ambiente local.
