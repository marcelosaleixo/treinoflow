# ETAPA 74 — Recomendação Adaptativa de Ações

## Objetivo
Usar o histórico real das ações de retenção do próprio Personal para recomendar a ação mais eficaz por faixa de risco.

## Regra
- Histórico considerado: últimos 90 dias.
- Faixas: CRÍTICO (75–100), ALTO (50–74), MÉDIO (25–49).
- Uma ação só é considerada evidência forte quando possui pelo menos 3 resultados finais na mesma faixa de risco.
- Sucesso = RECUPERADO ou RENOVADO.
- Se não houver evidência suficiente, o sistema não afirma que uma ação é melhor; usa o canal disponível como recomendação de baixa confiança.

## Rotas
- `/assistente/recomendacoes`
- O Assistente `/assistente/acoes` passa a exibir a recomendação adaptativa por aluno.

## Banco
Nenhuma migration nova. Reutiliza `acoes_assistente`.
