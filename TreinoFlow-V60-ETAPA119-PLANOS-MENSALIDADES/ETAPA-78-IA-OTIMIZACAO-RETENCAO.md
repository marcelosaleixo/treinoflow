# ETAPA 78 — IA de Otimização da Retenção

## Objetivo
Transformar o histórico de ações de retenção em recomendações adaptativas transparentes, sem depender de modelo externo de IA.

## Entregas
- Dashboard `/assistente/otimizacao`.
- Análise dos últimos 180 dias.
- Melhor ação por faixa de risco: CRÍTICO, ALTO e MÉDIO.
- Melhor horário por faixa de risco.
- Confiança baseada no tamanho da amostra.
- Mínimo de 3 resultados finais antes de aprender uma estratégia.
- Integração com `RecomendacaoAdaptativaService`: quando existe histórico suficiente, a estratégia aprendida passa a ter prioridade.
- Isolamento por `personalId`.

## Regra de aprendizado
Sucesso = RECUPERADO ou RENOVADO.
Resultado final = RECUPERADO, RENOVADO, CANCELAMENTO, SEM_RESPOSTA ou OUTRO.

Não há aprendizado de "etapa da jornada" nesta etapa porque `AcaoAssistente` mantém o estado atual da jornada na mesma linha; usar esse campo como histórico de etapas poderia gerar conclusões incorretas.
