# Etapa 196 — Testes automatizados da fila de e-mails

## Objetivo

Criar testes unitários para validar as transições de estado da entidade `EntregaAlertaEmail`, cobrindo a fila persistente introduzida nas etapas 192–195.

## Cobertura adicionada

Arquivo: `src/test/java/com/marceloaleixo/treinoflow/entity/EntregaAlertaEmailTest.java`

- Estado inicial pendente e pronta para processamento.
- Falha transitória, limpeza de reserva, intervalo crescente e truncamento da mensagem de erro.
- Limite de tentativas e transição para `FALHA_FINAL`.
- Sucesso e limpeza de erro/reserva.
- Recuperação de reserva expirada com e sem tentativas restantes.
- Reabertura manual de uma entrega em falha final.

## Como executar

Windows:

```bat
mvnw.cmd -Dtest=EntregaAlertaEmailTest test
```

Linux/macOS:

```bash
./mvnw -Dtest=EntregaAlertaEmailTest test
```

Para a suíte completa, use `mvnw.cmd test` ou `./mvnw test`.

## Limitações

Estes são testes unitários da máquina de estados da entidade. Eles não comprovam concorrência real entre duas instâncias, comportamento do PostgreSQL, envio SMTP real ou recuperação após reinicialização. Esses cenários exigem testes de integração com banco isolado e um servidor SMTP de teste. Não afirmar aprovação dos testes até executar Maven no ambiente de desenvolvimento/CI.
