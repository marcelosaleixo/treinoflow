# Etapa 127 — Exportação de Relatórios Financeiros

## Objetivo
Permitir que o Personal exporte os relatórios financeiros do próprio negócio em CSV, compatível com Excel e LibreOffice.

## Implementado
- `GET /financeiro/despesas/exportar?ano=AAAA&mes=MM`: lista despesas do mês selecionado, com data, descrição, categoria, valor, situação e observação.
- `GET /financeiro/dre/exportar?ano=AAAA&mes=MM`: exporta o resumo mensal da DRE simplificada.
- `GET /financeiro/previsao/exportar?meses=3|6|12`: exporta a projeção financeira para o horizonte solicitado (limite de 12 meses).
- Botões Exportar CSV nas telas de despesas, DRE e previsão financeira.
- CSV usa separador `;`, aspas para campos textuais e BOM UTF-8 para melhorar a abertura em planilhas brasileiras.
- Cada endpoint obtém o ID do Personal a partir da autenticação e usa consultas/serviços limitados a esse ID.

## Segurança e limitações
- Não exporta dados de outros Personais.
- Não altera registros, regras de cobrança ou banco de dados.
- A DRE exportada é gerencial e simplificada. A previsão não representa garantia de recebimento.
- Não foi possível confirmar compilação Maven neste ambiente; execute `mvn clean compile` antes do deploy.
