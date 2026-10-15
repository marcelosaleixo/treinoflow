# Etapa 208 — Recuperação e retentativas da fila de alertas

## Entrega
- Testes de integração com PostgreSQL 16 via Testcontainers.
- Verificação de reserva `PROCESSANDO` persistida e recuperada após o prazo expirar.
- Verificação do retorno para `PENDENTE`, limpeza da reserva e atualização do próximo horário.
- Verificação de `FALHA_FINAL` quando a reserva expira após atingir o limite de tentativas.

## Executar
Com Docker disponível:

```bash
mvn -Dtest=Etapa208RecuperacaoFilaPostgresIT test
```

Windows:

```bat
mvnw.cmd -Dtest=Etapa208RecuperacaoFilaPostgresIT test
```

## Limitações
Os testes exercitam a persistência e a transição de estado usando PostgreSQL real em contêiner. Não reiniciam um processo Spring Boot completo e não simulam a interrupção exatamente durante uma chamada SMTP. Os testes estão adicionados, mas precisam ser executados no ambiente de desenvolvimento.
