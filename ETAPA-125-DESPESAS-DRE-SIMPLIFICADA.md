# Etapa 125 — Despesas e DRE simplificada

## Objetivo
Completar o Financeiro do Personal com registro de despesas e visão gerencial mensal.

## Entregas
- Cadastro de despesas por Personal, com descrição, categoria, valor, data, status paga/pendente e observação.
- Listagem mensal e totais pagos/pendentes.
- DRE gerencial: receitas recebidas, despesas pagas, resultado simplificado e despesas pendentes.
- Proteção de propriedade por Personal nas consultas e exclusões.
- Entidade `DespesaPersonal`, tabela `despesas_personal`, criada pelo mecanismo de schema configurado no projeto (`ddl-auto=update`).

## Rotas
- `GET/POST /financeiro/despesas`
- `POST /financeiro/despesas/{id}/excluir`
- `GET /financeiro/dre`

## Regras
- Valores precisam ser positivos; descrição, categoria e data são obrigatórias.
- A DRE usa receita recebida (contas pagas por data de pagamento) menos despesas pagas por data da despesa.
- Despesas pendentes aparecem em separado e não reduzem o resultado até serem marcadas como pagas.
- A exclusão valida o vínculo com o Personal autenticado.
- Relatório é gerencial e não substitui contabilidade formal.

## Observação técnica
O projeto está configurado com `spring.jpa.hibernate.ddl-auto=update`; em produção, revisar a estratégia de migração de schema conforme o padrão do deploy.
