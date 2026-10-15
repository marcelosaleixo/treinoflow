# Correção de imports dos testes — Spring Boot 4

## Causa

O projeto usa Spring Boot 4.1.1. Os testes ainda importavam `@WebMvcTest` e `@DataJpaTest` dos pacotes antigos de Spring Boot 3, por isso o IDE marcava as anotações como não resolvidas.

## Correções

- `org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest` → `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`
- `org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest` → `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`
- Mantida a anotação `@MockitoBean` do Spring Framework para substituição de beans em testes.

## Validação

Execute na raiz:

```bash
mvn -DskipTests test-compile
```

Depois, execute os testes necessários. Os testes `*PostgresIT` dependem de Docker e Testcontainers. Esta correção de imports não significa que a suíte inteira tenha sido executada.
