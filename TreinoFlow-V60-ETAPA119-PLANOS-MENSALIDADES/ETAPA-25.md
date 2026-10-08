# TreinoFlow — Etapa 25: Validade configurável do link público

## Implementado
- O personal escolhe a validade do link ao liberar um treino: 7, 15, 30, 60, 90, 180 ou 365 dias.
- O padrão é 30 dias, preservando o comportamento anterior.
- O prazo escolhido é validado no serviço (1 a 365 dias) e usado para calcular `acessoExpiraEm`.
- A página pública continua recusando links expirados; não há alteração no esquema do banco.

## Teste manual
1. Abra um treino em rascunho com exercícios e escolha 7 dias para liberar.
2. Confira a data de expiração exibida no editor.
3. Teste a rota pública e confirme que o treino é exibido enquanto válido.
4. Confirme no serviço/teste automatizado que prazo nulo, menor que 1 ou maior que 365 é rejeitado.

## Validação
Execute `mvnw.cmd clean test` no Windows ou `./mvnw clean test` em ambiente com acesso ao Maven.
