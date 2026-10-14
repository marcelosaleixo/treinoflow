# Etapa 164 — Relatório completo de erros da validação CSV

## Objetivo
Permitir que o personal baixe os problemas encontrados na validação prévia do CSV, sem precisar copiar os erros exibidos na tela.

## Alterações
- Guarda os erros da validação na sessão autenticada.
- Adiciona `GET /alunos/importacao/validacao-erros.csv`.
- Gera CSV UTF-8 com BOM e colunas `Linha;Motivo`, escapando aspas e delimitadores de CSV.
- Exibe o botão de download apenas quando a validação encontrou problemas.
- Remove o relatório da sessão após o download e limpa um relatório antigo ao iniciar nova validação.
- Replica as alterações na cópia interna do projeto presente no ZIP.

## Testes recomendados
1. Validar um CSV sem erros: não deve aparecer botão de relatório de erros.
2. Validar um CSV com CPFs duplicados ou datas inválidas: deve aparecer o botão.
3. Baixar o CSV e conferir as colunas `Linha` e `Motivo`.
4. Validar novamente outro arquivo: o relatório anterior deve ser substituído.

## Validação técnica
ZIP verificado quanto à integridade. A compilação Maven e os testes de integração não foram executados neste ambiente.
