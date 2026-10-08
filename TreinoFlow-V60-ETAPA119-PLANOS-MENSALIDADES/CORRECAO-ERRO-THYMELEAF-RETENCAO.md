# Correção — Thymeleaf Retenção

## Erro

A página `templates/retencao/index.html` falhava ao renderizar com:

`Could not parse as expression: "'Vencido · ' + #temporals.format(i.dataProximaAcao,'dd/MM/yyyy')"`

## Causa

As expressões `th:text` da fila de retenção estavam usando expressão direta sem o delimitador `${...}` em uma composição que incluía `#temporals.format(...)`. Também havia uma expressão no cabeçalho misturando `${personal.nome}` dentro de outra expressão.

## Correção

As expressões foram normalizadas para o formato completo `${...}`:

- saudação do Personal;
- data de follow-up vencido;
- data de follow-up de hoje;
- próxima ação.

Nenhuma regra de negócio ou estrutura de banco foi alterada.

## Validação

- arquivo HTML revisado;
- expressões `th:text` verificadas;
- ZIP validado com `unzip -t`;
- build Maven não executado porque não há Maven CLI instalado no ambiente e o Maven Wrapper depende de download externo.
