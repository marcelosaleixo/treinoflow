# ETAPA 91 — Data de nascimento do aluno

## Objetivo
Adicionar a data de nascimento ao cadastro do aluno para permitir cálculo automático da idade e futuras personalizações por faixa etária.

## Implementação
- `Aluno.dataNascimento` como `LocalDate`.
- Coluna `data_nascimento`.
- `Aluno.getIdade()` calcula a idade atual sem persistir um segundo valor.
- O serviço rejeita datas futuras.
- O formulário exibe o campo com limite máximo na data atual.
- Na edição, a idade calculada aparece abaixo do campo.

## Banco
O projeto atual utiliza atualização automática do schema; a coluna será criada pelo Hibernate em ambientes configurados para `ddl-auto=update`.
