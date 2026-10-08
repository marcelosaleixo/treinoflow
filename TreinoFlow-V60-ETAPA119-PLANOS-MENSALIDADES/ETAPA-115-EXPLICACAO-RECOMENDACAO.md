# ETAPA 115 — Explicação da Recomendação

## Objetivo
Tornar a recomendação da Etapa 114 transparente e auditável para o Personal, mostrando quais dados e contexto sustentam a indicação.

## Implementação
- Novo `RecomendacaoExplicacaoView`.
- Novo `RecomendacaoExplicacaoService`.
- O fluxo de execução da ação do Radar passa a montar uma explicação junto da recomendação.
- Exibe objetivo do aluno, faixa etária, faixa de risco, base comparada, janela histórica, ação indicada, efetividade e força da evidência.
- Mostra a amostra utilizada e explica que a recomendação pode mudar com novos resultados finais.
- Força da evidência é apresentada de forma conservadora:
  - forte: 10+ resultados finais;
  - moderada: 5–9;
  - inicial: 3–4;
  - insuficiente: menos de 3.

## Limites
- Não cria nova tabela nem migration.
- Não envia mensagens automaticamente.
- Não executa ações automaticamente.
- Não altera treino, carga ou prescrição.
- O isolamento continua sendo por `personalId`.

## Fluxo
`Radar → recomendação → explicação dos dados → decisão humana → CRM → resultado → aprendizado`

## Benefício
O Personal consegue entender por que uma ação foi sugerida e quais informações podem fazer a recomendação mudar, reduzindo o efeito de uma “caixa-preta” na tomada de decisão.
