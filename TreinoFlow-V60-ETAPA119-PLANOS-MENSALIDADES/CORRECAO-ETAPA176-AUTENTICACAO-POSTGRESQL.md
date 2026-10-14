# Correção de autenticação PostgreSQL — Etapa 176

## Causa raiz

O log `The server requested SCRAM-based authentication, but no password was provided`
significa que o PostgreSQL respondeu, mas a aplicação enviou a conexão sem senha.
O erro de dialect do Hibernate é consequência da falha ao obter os metadados JDBC;
não é a causa inicial.

## Configuração no EasyPanel

No serviço **TreinoFlow (aplicação Java)**, abra Environment Variables e configure:

- `SPRING_DATASOURCE_URL` = `jdbc:postgresql://NOME_INTERNO_DO_SERVICO_POSTGRES:5432/NOME_DO_BANCO`
- `SPRING_DATASOURCE_USERNAME` = usuário existente no PostgreSQL
- `SPRING_DATASOURCE_PASSWORD` = senha real desse usuário
- `NIXPACKS_JDK_VERSION` = `21`

Use o hostname interno e a porta interna do serviço PostgreSQL. Não use `localhost` para conectar a outro container.

Compatibilidade: o projeto também aceita `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`. A senha tem fallback para `POSTGRES_PASSWORD`, mas essa variável só funciona se estiver definida também no ambiente do serviço da aplicação; variáveis de outro container não são compartilhadas automaticamente.

Depois de salvar, faça redeploy/rebuild do serviço TreinoFlow. Não coloque senhas reais no Git nem no arquivo `.env.example`.

## Configuração local

Defina `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` no ambiente local, com os valores correspondentes à instalação PostgreSQL local.

## Importante

Esta alteração corrige o mapeamento das variáveis e deixa o Hibernate reconhecer o dialeto PostgreSQL mesmo quando o banco está inacessível. Ela não cria nem recupera a senha do banco: a senha configurada na aplicação precisa corresponder à senha do usuário no PostgreSQL.
