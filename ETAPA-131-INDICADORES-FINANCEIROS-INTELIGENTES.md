# Etapa 131 — Indicadores Financeiros Inteligentes

## Entregas
- Nova tela `/financeiro/indicadores`.
- Comparação de receita recebida, despesas pagas e resultado contra o mês anterior.
- Progresso da meta mensal de receita configurada na Etapa 130.
- Exibição do saldo de contas marcadas como atrasadas.
- Recomendações gerenciais simples com base nos dados registrados: distância até a meta, limite de despesas, variação de receita e resultado negativo/decrescente.
- Atalho para os indicadores no dashboard financeiro.

## Regras e limitações
- Os cálculos são feitos por Personal autenticado e usam seus próprios lançamentos.
- O painel não usa IA externa nem envia mensagens automaticamente.
- A comparação percentual é indicativa quando o mês anterior não possui base de receita/despesa.
- Atrasos refletem contas cujo status está marcado como `ATRASADA`.
- Não foi adicionada migração de banco de dados.

## Validação
O arquivo ZIP foi verificado quanto à integridade do pacote. A compilação Maven e a execução completa da aplicação precisam ser confirmadas no ambiente do projeto antes da publicação.
