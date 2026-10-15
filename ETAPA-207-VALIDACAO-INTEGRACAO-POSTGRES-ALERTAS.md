# Etapa 207 — Validação de integração dos alertas

## Objetivo

Validar no PostgreSQL real duas garantias importantes da fila de e-mails: unicidade de entrega por incidente e reivindicação condicional de uma entrega pendente.

## Testes adicionados

Classe `Etapa207FilaAlertasPostgresIT`:

- `restricaoUnicaImpedeDuasEntregasParaMesmoIncidente`: confirma que a restrição única de `incidente_id` rejeita uma segunda linha para o mesmo incidente.
- `apenasUmaReivindicacaoCondicionalConsegueReservarEntrega`: confirma que a primeira atualização condicional reivindica a entrega e a segunda não altera uma linha já em `PROCESSANDO`.

## Execução

Requisitos: JDK 21, Maven e Docker em execução.

Linux/macOS:

```bash
./mvnw -Dtest=Etapa207FilaAlertasPostgresIT test
```

Windows:

```bat
mvnw.cmd -Dtest=Etapa207FilaAlertasPostgresIT test
```

Também é possível usar `mvn` se estiver instalado.

## Limitações

- Os testes usam Testcontainers com PostgreSQL 16; sem Docker, o teste é ignorado pela configuração `disabledWithoutDocker = true`.
- A segunda reivindicação sequencial verifica o predicado de estado; não substitui um teste de estresse com múltiplos processos/instâncias.
- Não comprova entrega SMTP exatamente uma vez, nem valida a aplicação inteira com o agendador de produção.
- A compilação e os testes precisam ser executados no ambiente de desenvolvimento antes da implantação.
