# Etapa 205 — Testes automatizados de idempotência dos alertas

## Objetivo
Validar que chamadas repetidas de enfileiramento para o mesmo incidente não criam entregas adicionais e que a restrição única do PostgreSQL é tratada corretamente em condições de concorrência.

## Cobertura adicionada
- Repetição do mesmo incidente: somente uma gravação esperada.
- Identificador de incidente vazio: rejeitado antes de acessar o repositório.
- Destinatário vazio: operação ignorada.
- Violação da restrição única com registro concorrente existente: tratada como duplicidade.
- Violação de integridade sem registro concorrente correspondente: exceção propagada.

## Executar
Na raiz do projeto:

```bash
mvn -Dtest=FilaAlertaEmailIdempotenciaTest test
```

No Windows:

```bat
mvnw.cmd -Dtest=FilaAlertaEmailIdempotenciaTest test
```

## Limites
Estes testes usam mocks para validar a lógica do serviço. Eles não substituem o teste concorrente com PostgreSQL real (`EntregaAlertaEmailConcorrenciaIT`) nem garantem entrega SMTP exatamente uma vez em caso de falha entre o aceite SMTP e a gravação do resultado.
