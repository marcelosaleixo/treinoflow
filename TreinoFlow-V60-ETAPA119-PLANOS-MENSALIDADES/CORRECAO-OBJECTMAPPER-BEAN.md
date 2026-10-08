# Correção — ObjectMapper não disponível como Bean

## Erro

O Spring encontrava a classe `com.fasterxml.jackson.databind.ObjectMapper`, mas não encontrava um bean desse tipo para injetar no construtor de `MercadoPagoGateway`:

`No qualifying bean of type 'com.fasterxml.jackson.databind.ObjectMapper' available`

## Causa

O `jackson-databind` já estava no `pom.xml`, porém o projeto usa `spring-boot-starter-webmvc` e não deve depender da criação automática do `ObjectMapper`.

## Correção

Foi criada a configuração `JacksonConfig`, que registra explicitamente um `ObjectMapper` como bean Spring usando `JsonMapper`.

Isso mantém o `MercadoPagoGateway` desacoplado da configuração automática e resolve a injeção:

`PagamentoController -> MercadoPagoGateway -> ObjectMapper`

## Após atualizar o projeto

No Windows:

```powershell
.\mvnw.cmd clean package
```

ou:

```powershell
.\mvnw.cmd spring-boot:run
```

Se estiver usando IntelliJ/Eclipse/STS, faça Reload/Reimport do Maven e reinicie a aplicação.
