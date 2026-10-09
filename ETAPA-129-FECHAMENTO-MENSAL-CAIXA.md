# Etapa 129 — Fechamento Mensal do Caixa

## Objetivo
Consolidar, por Personal e por mês, recebimentos registrados, recebimentos conciliados, pagamentos ainda não conciliados, despesas pagas e despesas pendentes.

## Entregas
- Nova tela `/financeiro/fechamento` com filtro mensal.
- Resumo de valores e quantidades de pagamentos conciliados e pendentes.
- Saldo operacional simplificado calculado como recebimentos registrados menos despesas pagas.
- Checklist manual com links para conciliação, despesas e DRE.
- Consulta limitada ao Personal autenticado.

## Regras e limites
- Nenhuma baixa ou alteração financeira é executada pelo fechamento.
- Não há integração bancária nem confirmação automática de transações.
- A tela é um resumo gerencial, não um fechamento contábil formal.
- Não foi criada tabela nem migração nova; os dados vêm dos pagamentos, conciliações e despesas já existentes.
- A quantidade de pagamentos conciliados é contada por identificador de pagamento, e o total conciliado é somado pelo valor do pagamento.

## Validação
ZIP validado estruturalmente. A compilação Maven precisa ser executada no ambiente do projeto antes do deploy.
