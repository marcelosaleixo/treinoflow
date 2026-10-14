# Etapa 170 — Relatório de resultado da importação CSV

- Apresenta o total de linhas analisadas, alunos importados e linhas ignoradas/rejeitadas.
- Gera CSV detalhado por linha com número, nome, e-mail, resultado (`IMPORTADO`, `IGNORADO` ou `REJEITADO`) e motivo.
- O arquivo é gerado por sessão e não altera os dados do aluno.
- O relatório é disponibilizado ao final da importação.

## Validação recomendada

Testar importação com aluno válido, e-mail duplicado, CPF já cadastrado, campos inválidos e linha com quantidade incorreta de colunas. Conferir os contadores e o CSV final.
