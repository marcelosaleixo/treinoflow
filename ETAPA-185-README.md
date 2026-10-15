# TreinoFlow — Etapa 185

Esta versão continua a Etapa 184 com preferências individuais de notificações em tempo real.

Leia `ETAPA-185-PREFERENCIAS-NOTIFICACOES.md` para detalhes, rotas e roteiro de validação.

## Execução
- Java 21
- Configure as variáveis de ambiente do PostgreSQL conforme o ambiente atual.
- Windows: `mvnw.cmd test`
- Linux: `./mvnw test`
- Publique somente após confirmar compilação, testes e a tela autenticada de preferências.

A entidade é criada pelo Hibernate em ambientes que usam `spring.jpa.hibernate.ddl-auto=update`. Para ambientes com migrações controladas, revise o SQL de referência em `src/main/resources/db/migration/V185__preferencias_notificacao_tempo_real.sql`.
