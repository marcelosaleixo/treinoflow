# Etapa 177 — Alertas de qualidade na importação CSV

## O que mudou
- O dashboard de importações mostra um alerta quando o percentual de linhas ignoradas/rejeitadas no período atinge o limite configurado.
- O relatório mensal identifica individualmente os meses que atingiram o limite e exibe um aviso geral.
- O limite é configurável por variável de ambiente, sem alteração do banco de dados.
- O alerta considera `(ignorados + rejeitados) / linhas analisadas * 100`; períodos/meses sem linhas analisadas não geram alerta.

## Configuração
- Variável de ambiente: `TREINOFLOW_IMPORTACAO_LIMITE_PROBLEMAS_PERCENTUAL`
- Padrão: `10` (alerta em 10% ou mais).
- Exemplo: `TREINOFLOW_IMPORTACAO_LIMITE_PROBLEMAS_PERCENTUAL=15` para alertar a partir de 15%.

## Telas
- `/alunos/importacao/dashboard`
- `/alunos/importacao/relatorio-mensal`

Os dados continuam limitados ao personal autenticado. Este alerta é exibido dentro do sistema; não envia e-mail ou WhatsApp.
