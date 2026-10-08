# ETAPA 87 — IA DE PRESCRIÇÃO ASSISTIDA

## Objetivo
Transformar as análises de progressão, multi-série e tendência em uma sugestão objetiva para a próxima prescrição, mantendo a decisão final com o Personal.

## Regras
- Não altera banco nem prescrição automaticamente.
- Alta confiança + progressão + esforço controlado: sugere pequeno incremento de carga (2,5% como referência).
- Atenção/fadiga/queda: mantém carga atual e recomenda reavaliação.
- Tendência positiva média: consolida antes de progredir.
- Poucos dados: coleta mais histórico.
- Séries e repetições permanecem baseadas na prescrição atual.

## Integração
A sugestão aparece em `performance/aluno-evolucao.html` por exercício.
