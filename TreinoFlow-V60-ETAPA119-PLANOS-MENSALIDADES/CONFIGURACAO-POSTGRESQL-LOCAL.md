# Correcao PostgreSQL local — TreinoFlow V137

## Causa do erro

`The server requested SCRAM-based authentication, but no password was provided` significa que a aplicacao chegou ao PostgreSQL, mas a propriedade `spring.datasource.password` ficou vazia. O Hibernate nao consegue obter os metadados JDBC e, por isso, tambem informa que nao conseguiu determinar o dialeto.

A senha real do banco nao pode ser descoberta nem criada pelo codigo. Ela precisa ser informada pelo desenvolvedor.

## Executar localmente no Windows

1. Abra o PowerShell na pasta do projeto que contem `mvnw.cmd`.
2. Execute:

   ```powershell
   Set-ExecutionPolicy -Scope Process Bypass
   .\executar-local.ps1
   ```

3. Informe URL JDBC, usuario e senha do PostgreSQL. A senha e solicitada sem eco e nao e gravada em arquivo.
4. O script inicia a aplicacao via Maven Wrapper, passando as variaveis apenas ao processo atual.

URL padrao sugerida: `jdbc:postgresql://localhost:5432/treinoflow`. Confirme que o banco e o usuario existem e use os valores corretos da sua instalacao.

## Se iniciar pelo IntelliJ/Eclipse/VS Code

Configure na Run Configuration as variaveis de ambiente:

- `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/treinoflow`
- `SPRING_DATASOURCE_USERNAME=seu_usuario`
- `SPRING_DATASOURCE_PASSWORD=sua_senha_real`

Nao compartilhe nem versione a senha. No EasyPanel, configure as mesmas variaveis no servico da aplicacao usando as credenciais reais do banco e o hostname interno do servico PostgreSQL.

## Diagnostico

Se o erro continuar, verifique se `SPRING_DATASOURCE_PASSWORD` esta realmente definida no processo que inicia a aplicacao, se a senha corresponde ao usuario e se URL/porta/banco estao corretos. Nao e recomendado colocar uma senha presumida como valor padrao no `application.properties`.
