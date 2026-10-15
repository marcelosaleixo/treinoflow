# Etapa 199 — Teste de concorrência da fila de e-mails

## Objetivo
Validar, em PostgreSQL real, que duas threads que tentam reivindicar simultaneamente a mesma entrega não obtêm ambas a reserva.

## Implementação
- Novo teste `EntregaAlertaEmailConcorrenciaIT` com Testcontainers e PostgreSQL 16.
- Duas tarefas aguardam uma barreira e executam a atualização condicional `reivindicarAtomico` simultaneamente.
- A soma dos resultados deve ser exatamente `1`; a entrega deve terminar como `PROCESSANDO`.
- O teste é ignorado automaticamente quando Docker não está disponível.

## Execução
```bash
mvn -Dtest=EntregaAlertaEmailConcorrenciaIT test
```
No Windows, se o wrapper estiver no projeto:
```bat
mvnw.cmd -Dtest=EntregaAlertaEmailConcorrenciaIT test
```

## Limitações
O teste verifica a reivindicação simultânea da mesma linha no PostgreSQL. Não comprova entrega SMTP exatamente uma vez: uma queda após o aceite SMTP e antes de gravar o sucesso ainda pode causar reenvio. Também não substitui teste de carga com várias instâncias e broker/infraestrutura de produção.
