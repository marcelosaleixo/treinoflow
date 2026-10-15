# Etapa 202 — Teste ponta a ponta da fila de alertas

## Objetivo

Combinar PostgreSQL real (via Testcontainers) e SMTP simulado (GreenMail) no mesmo cenário de integração. O teste grava a entrega, relê o registro, envia o e-mail de teste, valida destinatário/assunto e persiste o estado `ENVIADO`. Um segundo cenário verifica que a falha pode ser persistida e reagendada.

## Arquivo

`src/test/java/com/marceloaleixo/treinoflow/integration/EntregaAlertaEmailE2EPostgresIT.java`

## Pré-requisitos

- Java compatível com a versão do projeto (Java 21 recomendado).
- Docker ativo, para iniciar PostgreSQL 16 pelo Testcontainers.
- Maven e acesso às dependências de teste.
- O teste SMTP usa GreenMail local; não envia mensagens externas.

## Executar

```bash
mvn -Dtest=EntregaAlertaEmailE2EPostgresIT test
```

No Windows, se o wrapper estiver disponível:

```bat
mvnw.cmd -Dtest=EntregaAlertaEmailE2EPostgresIT test
```

## Limites

Este teste integra persistência, serviço de e-mail e transição de estado, mas não inicia o agendador de produção nem prova o comportamento SMTP exatamente uma vez em caso de queda entre o aceite SMTP e a gravação no banco. A execução real dos testes deve ser confirmada no ambiente do projeto.
