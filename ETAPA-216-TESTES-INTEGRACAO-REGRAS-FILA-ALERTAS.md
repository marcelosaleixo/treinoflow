# Etapa 216 — Testes de integração das regras da fila de alertas

## Objetivo

Validar contra PostgreSQL real as regras de reabertura de entregas de alerta por e-mail, a persistência da transição de estado e a preservação da idempotência por incidente.

## Cobertura adicionada

Classe: `Etapa216ReenvioRegrasFilaPostgresIT`

- Uma entrega em `FALHA_FINAL` pode ser reaberta e volta para `PENDENTE` com tentativas e dados transitórios reiniciados.
- Uma entrega `PENDENTE` não pode ser reaberta novamente.
- A reabertura não cria uma segunda entrega para o mesmo incidente.

## Execução

Com Docker ativo:

```bash
mvn -Dtest=Etapa216ReenvioRegrasFilaPostgresIT test
```

Windows com Maven Wrapper:

```powershell
.\mvnw.cmd -Dtest=Etapa216ReenvioRegrasFilaPostgresIT test
```

O Testcontainers inicia PostgreSQL 16 em contêiner. O teste não envia e-mails reais; o serviço SMTP é simulado. A execução deve ser confirmada no ambiente do projeto antes de considerar a etapa validada.
