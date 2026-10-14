# Etapa 171 — Reimportação de linhas pendentes CSV

## Objetivo
Facilitar a correção dos registros que não foram importados, sem precisar editar novamente todo o arquivo original.

## Alterações
- Novo endpoint `GET /alunos/importacao/linhas-pendentes.csv`.
- O relatório usa o resultado da importação guardado na sessão autenticada.
- Inclui somente registros `IGNORADO` ou `REJEITADO`; registros `IMPORTADO` são excluídos.
- Preserva os nove campos originais do modelo CSV para edição e reenvio.
- Adicionado botão na tela de importação, após a conclusão.
- Mantida a exportação existente do resultado completo.

## Como testar
1. Importe um CSV contendo um aluno válido, uma duplicidade e um registro inválido.
2. Baixe `linhas-pendentes-importacao-alunos.csv`.
3. Confirme que somente as linhas ignoradas/rejeitadas estão presentes e que os nove campos estão preservados.
4. Corrija os dados ou remova a duplicidade e importe novamente.

## Observação
A exportação depende do relatório da última importação armazenado na sessão. A compilação e os testes de integração devem ser executados no ambiente do projeto antes da publicação.
