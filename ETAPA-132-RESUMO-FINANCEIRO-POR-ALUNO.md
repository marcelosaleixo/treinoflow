# Etapa 132 — Resumo financeiro por aluno

## Entregas
- Nova tela em `/financeiro/por-aluno`, com filtro por ano e mês.
- Agrupamento dos lançamentos de contas a receber por aluno, limitado ao Personal autenticado.
- Exibe valores recebidos no mês selecionado, pendências e valores em atraso.
- Totais gerais e tabela responsiva.
- Atalho incluído no dashboard financeiro.
- Sem nova tabela ou migração de banco de dados.

## Limites do indicador
O sistema ainda não associa despesas gerais a alunos individualmente. Por isso, esta etapa não calcula nem apresenta lucro/margem líquida por aluno; apresenta receita e contas a receber associadas ao aluno para não atribuir custos arbitrariamente.

## Validação
O arquivo ZIP deve ser validado pela integridade do arquivo. A compilação e execução Maven precisam ser confirmadas no ambiente de desenvolvimento antes da publicação.
