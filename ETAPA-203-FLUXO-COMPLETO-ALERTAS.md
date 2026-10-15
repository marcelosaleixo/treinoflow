# Etapa 203 — Fluxo completo de alertas operacionais por e-mail

## Objetivo

Adicionar um teste de integração que valida o ciclo persistência da entrega, envio para SMTP simulado e gravação do estado final no PostgreSQL.

## Arquivo adicionado

`src/test/java/com/marceloaleixo/treinoflow/integration/FluxoCompletoEntregaAlertaEmailIT.java`

## Cenário coberto

1. Persiste uma entrega no estado `PENDENTE` no PostgreSQL 16 via Testcontainers.
2. Envia o conteúdo para um servidor SMTP simulado GreenMail.
3. Confere destinatário e assunto recebidos pelo servidor simulado.
4. Persiste a transição para `ENVIADO`.
5. Relê a entrega do banco e verifica `enviadaEm`, `reservadoAte` e `ultimoErro`.

## Execução

Com Docker ativo e Maven disponível:

```bash
mvn -Dtest=FluxoCompletoEntregaAlertaEmailIT test
```

No Windows:

```bat
mvnw.cmd -Dtest=FluxoCompletoEntregaAlertaEmailIT test
```

## Limitações

Este teste valida o ciclo de persistência e SMTP simulado, mas invoca explicitamente o serviço de e-mail e a transição da entidade. Não executa o agendador real de produção, nem prova entrega exatamente uma vez em caso de queda após o aceite SMTP. O teste requer Docker; com Docker indisponível, Testcontainers pode ignorá-lo conforme configuração.
