# Etapa 141 — Conversão financeira das ações de CRM

## Entrega
- Nova tela `/financeiro/conversao-acoes` com filtro por mês e ano.
- Indicadores de ações registradas, alunos alcançados, taxa de conversão registrada e receita atribuída.
- Comparação por tipo de ação: recuperações, renovações, cancelamentos, taxa de conversão e pagamentos vinculados.
- Histórico de até 50 ações do período e atalho no painel financeiro.

## Critérios
- O período de seleção filtra pela data do contato da ação (`dataContato`).
- Receita atribuída considera pagamentos vinculados às ações realizadas no período, mesmo que a data do pagamento seja posterior ou anterior ao mês da ação.
- A taxa de conversão é (recuperados + renovados) / (recuperados + renovados + cancelamentos). Quando não há ações encerradas, o indicador é exibido como indisponível.
- A receita atribuída não representa lucro líquido e não prova que a ação causou o pagamento.
- Os dados são filtrados pelo `personalId` do usuário autenticado.

## Validação
O arquivo ZIP é validado quanto à integridade. A compilação Maven e o teste integrado com PostgreSQL precisam ser executados no ambiente do projeto; não declarar a etapa como compilada/testada até realizar essas verificações.
