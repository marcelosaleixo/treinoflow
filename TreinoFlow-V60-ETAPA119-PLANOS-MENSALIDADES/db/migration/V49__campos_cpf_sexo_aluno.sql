-- Adiciona dados cadastrais opcionais ao aluno.
-- CPF é armazenado apenas com dígitos e validado pela aplicação.
ALTER TABLE alunos ADD COLUMN IF NOT EXISTS cpf VARCHAR(11);
ALTER TABLE alunos ADD COLUMN IF NOT EXISTS sexo VARCHAR(20);
