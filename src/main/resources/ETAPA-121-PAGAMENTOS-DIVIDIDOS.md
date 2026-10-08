# Etapa 123 — Pagamentos divididos

Uma conta a receber agora pode ser quitada com mais de uma forma de pagamento.

Exemplo: mensalidade de R$ 300,00 → R$ 100,00 em dinheiro + R$ 200,00 em PIX.

## Regras
- A soma dos pagamentos deve ser exatamente igual ao valor da conta.
- Cada forma de pagamento pode aparecer uma única vez na mesma quitação.
- O sistema permite adicionar quantas formas forem necessárias.
- O campo legado `forma_pagamento` continua preenchido quando existe somente uma forma; em pagamentos divididos, o detalhamento fica em `contas_receber_pagamentos`.
- Não altera a regra de isolamento por Personal.
