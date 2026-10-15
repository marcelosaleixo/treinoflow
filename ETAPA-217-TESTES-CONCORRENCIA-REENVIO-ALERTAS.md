# Etapa 217 — Concorrência e idempotência no reenvio de alertas

## Objetivo

Verificar com PostgreSQL real que duas solicitações administrativas concorrentes não reabrem a mesma entrega duas vezes e que novas solicitações são recusadas depois que a entrega volta para `PENDENTE`.

## Cobertura adicionada

- `Etapa217ReenvioConcorrenciaPostgresIT`: duas transações concorrentes usam `SELECT ... FOR UPDATE` por meio de `buscarParaAtualizacao`; exatamente uma pode transicionar `FALHA_FINAL` para `PENDENTE`.
- Verifica que a linha continua única por incidente e que a reabertura limpa tentativas, reserva e erro anterior.
- Verifica que chamadas repetidas após a primeira reabertura retornam falso e não criam novas entregas.
- O teste usa Testcontainers com PostgreSQL 16 e não envia e-mails reais.

## Execução

```bash
mvn -Dtest=Etapa217ReenvioConcorrenciaPostgresIT test
```

Windows com Maven Wrapper:

```powershell
.\mvnw.cmd -Dtest=Etapa217ReenvioConcorrenciaPostgresIT test
```

Docker deve estar instalado e em execução. O teste é ignorado automaticamente quando o Docker não está disponível, conforme a configuração Testcontainers.

## Limites

O teste exercita a mesma sequência transacional de bloqueio e transição de estado usada pelo serviço. Não inicia a aplicação web completa nem simula um clique HTTP simultâneo no controlador; a autorização/CSRF permanece coberta pelos testes de controlador das etapas anteriores.
