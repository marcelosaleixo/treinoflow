# Correção — Dashboard de Performance

## Erro
`DashboardPerformanceService` chamava `buscarHistoricoDoPersonal(Long)`, mas o ambiente em execução estava com uma versão compilada de `RegistroTreinoAlunoRepository` que não continha esse método.

## Correção
O serviço passou a utilizar `findAll()`, método garantido pelo contrato `JpaRepository`, filtrando os registros pelo Personal dentro da transação. Isso elimina a dependência do método específico que causava o `Unresolved compilation problem`.

## Importante
Após substituir o projeto, faça uma compilação limpa para eliminar classes antigas:

```bash
./mvnw clean package -DskipTests
```

Se estiver usando IDE, execute `Clean/Rebuild` e reinicie a aplicação.
