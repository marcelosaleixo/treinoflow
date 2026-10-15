# Correção — Compatibilidade dos testes com Spring Boot 4

## Causa

O projeto utiliza Spring Boot 4.1.1. Nessa versão, os testes devem usar a API de substituição de beans do Spring Framework (`@MockitoBean`) em vez da antiga anotação `@MockBean`, que pertence ao pacote do Spring Boot e não está disponível nesta linha principal.

## Correção aplicada

- Substituídos os imports `org.springframework.boot.test.mock.mockito.MockBean` por `org.springframework.test.context.bean.override.mockito.MockitoBean`.
- Substituídas as anotações `@MockBean` por `@MockitoBean` nas classes de teste afetadas.
- Mantidos os mocks e os cenários de teste existentes; a mudança corrige a API usada para registrar os mocks no contexto de teste.

## Como validar

Na raiz do projeto:

```bash
mvn -DskipTests test-compile
mvn -Dtest=Etapa214SegurancaControladoresReaisTest test
mvn -Dtest=Etapa215AcessoAutorizadoRegrasNegocioTest test
mvn -Dtest=Etapa218FluxoHttpReenvioTest test
```

No Windows, pode-se trocar `mvn` por `./mvnw.cmd`.

A compilação e os testes precisam ser executados no ambiente de desenvolvimento para confirmar o resultado. Esta correção não declara os testes como aprovados sem essa execução.
