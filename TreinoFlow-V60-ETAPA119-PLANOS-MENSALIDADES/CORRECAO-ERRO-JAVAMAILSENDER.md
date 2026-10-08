# Correção — JavaMailSender ausente

## Causa
O `EmailNotificacaoService` depende de `org.springframework.mail.javamail.JavaMailSender`, mas a aplicação iniciou sem essa classe no classpath, causando `NoClassDefFoundError`.

## Correção
Mantido o `spring-boot-starter-mail` e adicionadas dependências explícitas de suporte:
- `org.springframework:spring-context-support`
- `org.eclipse.angus:jakarta.mail`

Isso garante a presença da API/infraestrutura necessária para o `JavaMailSender` no Spring Boot 4 / Spring Framework 7.

## Depois de substituir o projeto
Execute:

```powershell
.\mvnw.cmd clean
.\mvnw.cmd dependency:tree | Select-String "mail|context-support"
.\mvnw.cmd spring-boot:run
```

Se a IDE continuar mostrando classes antigas em `target/classes`, apague a pasta `target` antes de iniciar.
