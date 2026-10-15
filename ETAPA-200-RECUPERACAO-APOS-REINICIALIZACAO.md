# Etapa 200 — Recuperação após reinicialização da fila de e-mails

## Objetivo
Validar que uma entrega reservada como `PROCESSANDO` permanece no PostgreSQL e pode ser recuperada após a aplicação retomar a leitura dos dados.

## Cobertura adicionada
- Persistência do status `PROCESSANDO`, contador de tentativas e prazo da reserva.
- Recuperação de reserva expirada para `PENDENTE`, limpando `reservadoAte`.
- Transição para `FALHA_FINAL` quando a reserva expira após atingir o limite de tentativas.

## Execução
Com Docker disponível:

```bash
mvn -Dtest=EntregaAlertaEmailPostgresIT test
```

No Windows, se o wrapper estiver no projeto:

```bat
mvnw.cmd -Dtest=EntregaAlertaEmailPostgresIT test
```

## Limites
O teste simula retomada por meio de uma nova leitura do registro persistido; não encerra e relança um processo completo da aplicação. O Testcontainers precisa de Docker e os testes não foram executados durante a geração deste pacote. O cenário SMTP aceito antes de uma queda, mas sem confirmação no banco, continua sujeito a reenvio.
