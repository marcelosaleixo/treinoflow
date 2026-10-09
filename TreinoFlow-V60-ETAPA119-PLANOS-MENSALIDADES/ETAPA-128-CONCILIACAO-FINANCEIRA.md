# Etapa 128 — Conciliação Financeira

## Objetivo
Permitir que o Personal confira manualmente os pagamentos registrados no TreinoFlow contra extratos, comprovantes ou caixa físico.

## Funcionalidades
- Tela em `/financeiro/conciliacao` com filtro mensal.
- Lista os pagamentos registrados, incluindo pagamentos divididos por forma.
- Exibe data, aluno, descrição da conta, forma de pagamento, valor e situação da conciliação.
- Permite marcar cada pagamento como conciliado e registrar observação opcional.
- Impede a conciliação duplicada do mesmo pagamento por meio de validação de serviço e restrição única no banco.
- Todas as consultas e ações são limitadas ao Personal autenticado.

## Segurança e limites
- A conciliação é manual; não existe integração bancária nesta etapa.
- Marcar como conciliado não altera o estado da conta a receber nem cria/edita pagamentos.
- Não altera regras de mensalidade, recorrência ou pagamento dividido.
- A tabela `conciliacoes_financeiras_personal` é criada pelo Hibernate em ambientes com `ddl-auto=update`. Para produção com `validate`, deve-se adicionar migração equivalente antes do deploy.

## Arquivos
- `entity/ConciliacaoFinanceira.java`
- `repository/ConciliacaoFinanceiraRepository.java`
- `repository/PagamentoContaReceberRepository.java`
- `service/ConciliacaoFinanceiraService.java`
- `controller/ConciliacaoFinanceiraController.java`
- `templates/financeiro/conciliacao.html`
