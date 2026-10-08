# Etapa 12 — Entrada de altura e validação de avaliações

## Alterações
- O campo de altura aceita centímetros (ex.: `160`) ou metros (ex.: `1.60`).
- O service normaliza centímetros para metros antes de persistir.
- A entidade/banco continua usando `altura_metros` e valores canônicos como `1.60`.
- Alturas fora do intervalo de 0,50 m a 2,50 m são rejeitadas com mensagem clara.
- O formulário explica as unidades e apresenta mensagem de validação retornada pelo service.
- Mantido o padrão de injeção por atributo com `@Autowired`.

## Testes manuais sugeridos
1. Informar altura `160`: deve persistir como `1.60`.
2. Informar altura `1.60`: deve persistir como `1.60`.
3. Informar `260`, `0.20` ou valor negativo: deve ser recusado.
4. Conferir `altura_metros` no PostgreSQL após salvar.

A compilação e os testes integrados precisam ser executados no ambiente local com Maven e PostgreSQL.
