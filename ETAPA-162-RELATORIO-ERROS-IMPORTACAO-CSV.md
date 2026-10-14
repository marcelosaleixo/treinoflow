# Etapa 162 — Relatório de erros da importação CSV

## Objetivo
Permitir baixar um CSV com os erros e registros ignorados após importar alunos.

## Implementação
- Adiciona o endpoint `GET /alunos/importacao/resultado-erros.csv`.
- Gera relatório CSV UTF-8 com BOM e colunas `Linha` e `Motivo`.
- Mantém no máximo 10.000 ocorrências por sessão para limitar o uso de memória.
- A tela continua exibindo até 20 mensagens e oferece download do relatório completo quando houver erros.
- O relatório fica associado à sessão do usuário e é removido após o download.

## Testes recomendados
1. Importar CSV sem erros e confirmar que o link não aparece.
2. Importar CSV com CPF duplicado e linha inválida e baixar o relatório.
3. Conferir caracteres acentuados e abrir o CSV no Excel.
4. Confirmar que o endpoint não retorna relatório de uma sessão sem erros.

ZIP de origem: V120 / Etapa 161. A compilação Maven e o teste integrado devem ser executados antes da publicação.
