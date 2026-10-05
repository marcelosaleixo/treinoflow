# Correção — `NoClassDefFoundError: ObjectMapper`

## Causa

A `MercadoPagoGateway` recebe `com.fasterxml.jackson.databind.ObjectMapper` no construtor, mas o projeto não possuía `jackson-databind` explicitamente disponível no classpath.

Durante a criação do bean, o Spring precisava inspecionar os construtores de `MercadoPagoGateway` e a JVM não conseguiu carregar `ObjectMapper`.

## Correção

Foi adicionada ao `pom.xml` a dependência:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

A versão não foi fixada manualmente; ela é gerenciada pelo Spring Boot parent.

## Resultado esperado

O bean `mercadoPagoGateway` poderá ser criado e o `PagamentoController` poderá receber essa dependência normalmente.

O erro:

`java.lang.NoClassDefFoundError: ObjectMapper`

não deve mais ocorrer após atualizar as dependências e reconstruir o projeto.

## Após baixar

Execute no projeto:

```bash
./mvnw clean package
```

No Windows:

```powershell
.\mvnw.cmd clean package
```

Se estiver usando IntelliJ/Eclipse/VS Code, faça também um reload/reimport do Maven.
