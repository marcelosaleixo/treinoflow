# ETAPA 70 — Central de Inteligência do Personal

## Objetivo
Unificar performance, metas, gamificação, tendência e risco de alunos em uma única visão operacional.

## Rota
`GET /inteligencia`

## Implementação
- `CentralInteligenciaDashboardView`
- `CentralInteligenciaAcaoView`
- `CentralInteligenciaService`
- `CentralInteligenciaController`
- `templates/inteligencia/index.html`

## Reuso
A etapa não cria tabelas nem migração. Reutiliza:
- `MetaComercialService`
- `GamificacaoService`
- `TendenciaPerformanceService`
- `ScoreRiscoAlunoService`

## Decisão operacional
A central apresenta ações priorizadas a partir dos indicadores já existentes. Alunos em risco alto/crítico, ausência de contato por 30 dias, metas abaixo de 75% e tendência negativa recebem prioridade.

## Observação
A central não altera dados de domínio. Os módulos existentes continuam sendo a fonte dos indicadores e das ações executáveis.
