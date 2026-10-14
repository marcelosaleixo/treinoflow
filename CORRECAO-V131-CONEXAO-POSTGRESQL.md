# Correção V131 — Conexão PostgreSQL / HikariCP

## Causa identificada no log

`HikariPool ... total=0, active=0, idle=0` junto com `JDBCConnectionException` significa que o pool não conseguiu obter nem uma conexão JDBC. O `CannotCreateTransactionException` é consequência: o `TreinoFlowDataInitializer` tenta iniciar uma transação e o PostgreSQL não está acessível com a configuração atual.

Isso não é, por si só, um erro causado por `@Transactional`. A aplicação precisa conseguir acessar o PostgreSQL.

## Configuração no EasyPanel

Em **Environment Variables** do serviço TreinoFlow, configure com os valores reais do banco:

- `SPRING_DATASOURCE_URL=jdbc:postgresql://NOME_INTERNO_DO_SERVICO_POSTGRES:5432/NOME_DO_BANCO`
- `SPRING_DATASOURCE_USERNAME=USUARIO_REAL`
- `SPRING_DATASOURCE_PASSWORD=SENHA_REAL`

Use o nome interno do serviço PostgreSQL no EasyPanel, não `localhost` e não um IP público, a menos que o banco esteja realmente fora da rede interna e aceite conexões externas. O banco, usuário, senha e porta devem corresponder à configuração real do PostgreSQL.

Para execução local, configure as mesmas variáveis apontando para o PostgreSQL local, ou use `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` como alternativas legadas. A senha não deve ficar no `application.properties` nem ser enviada ao Git.

Depois de salvar as variáveis:
1. Confirme que o serviço PostgreSQL está ativo.
2. Confirme que o banco existe e que o usuário tem permissão.
3. Faça redeploy/rebuild do TreinoFlow.
4. Consulte os primeiros erros de conexão no log. `Connection refused` normalmente indica host/porta ou serviço indisponível; `timeout` indica rota/firewall/rede; `password authentication failed` indica credenciais; `database ... does not exist` indica nome de banco incorreto.

## Alterações nesta versão

- Removeu credenciais de banco hardcoded do `application.properties`.
- Prioriza `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`.
- Mantém compatibilidade com `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`.
- Define timeouts Hikari configuráveis para evitar espera de 30 segundos por padrão.
- Aplicado às duas cópias do projeto no ZIP.

A integridade do ZIP pode ser verificada, mas esta alteração não consegue corrigir credenciais, disponibilidade ou rede do PostgreSQL do ambiente de implantação. A compilação e o teste real de conexão devem ser executados no ambiente que contém o banco.
