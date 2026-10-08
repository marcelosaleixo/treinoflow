# ETAPA 73 — Aprendizado das Ações do Assistente

## Objetivo
Comparar os resultados históricos das ações executadas pelo Assistente para identificar qual abordagem apresenta melhor desempenho na carteira do Personal.

## Rota
`GET /assistente/aprendizado`

## Escopo
- Compara WhatsApp e Follow-up.
- Calcula taxa de sucesso por tipo de ação.
- Analisa desempenho por faixas de risco: crítico, alto e médio.
- Gera uma recomendação baseada exclusivamente nos resultados registrados.
- Reduz recomendações com amostra pequena.

## Regra de sucesso
Sucesso = Recuperado ou Renovado.
Taxa = (Recuperados + Renovados) / resultados finais registrados.

## Banco
Não cria tabela ou migration. Reutiliza `acoes_assistente` da Etapa 72.

## Limitação deliberada
O módulo não usa IA generativa e não afirma causalidade. Uma taxa histórica maior indica associação no histórico do Personal, não prova que a ação causou o resultado.
