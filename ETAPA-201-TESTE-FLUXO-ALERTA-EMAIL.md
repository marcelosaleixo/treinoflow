# Etapa 201 — Teste de integração do fluxo de alerta por e-mail

## Objetivo
Validar a interação entre o modelo de entrega persistível do domínio, as transições de estado e o envio SMTP contra GreenMail local, sem enviar mensagens para destinatários externos.

## Cobertura
- Estado inicial `PENDENTE`.
- Envio para SMTP simulado e conferência de assunto/destinatário.
- Transição para `ENVIADO` e registro de data de envio após aceite SMTP.
- Falha SMTP representada no domínio, reagendamento e registro do erro.
- Transição para `FALHA_FINAL` quando o limite de tentativas é atingido.

## Execução
Na raiz do projeto:

```bash
mvn -Dtest=FluxoEntregaAlertaEmailIT test
```

No Windows, se o wrapper estiver disponível:

```bat
mvnw.cmd -Dtest=FluxoEntregaAlertaEmailIT test
```

O teste usa GreenMail local e não exige credenciais SMTP reais. Ele não é um teste completo do agendador nem de transação PostgreSQL; essas partes permanecem cobertas separadamente pelos testes específicos da fila.

## Limitação importante
O aceite pelo SMTP não garante que o destinatário final leu a mensagem. Também existe uma janela de falha entre o aceite SMTP e a gravação do estado `ENVIADO`; por isso, a entrega exatamente uma vez não pode ser garantida apenas pelo cliente SMTP.
