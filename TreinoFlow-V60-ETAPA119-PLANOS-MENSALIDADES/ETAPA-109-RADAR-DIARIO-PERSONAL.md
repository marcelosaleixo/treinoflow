# ETAPA 109 — Radar Diário do Personal

## Objetivo
Consolidar em uma única tela os sinais operacionais que podem exigir atenção no dia: follow-ups vencidos/hoje, feedback pós-treino com sinais fortes e risco de abandono alto.

## Implementação
- `RadarDiarioService`: consolida dados existentes sem criar nova tabela.
- `RadarDiarioView`: visão de cada item priorizado.
- `RadarDiarioController`: rota `GET /performance/radar-diario`.
- `performance/radar-diario.html`: painel responsivo com resumo, prioridade #1 e fila de até 12 itens.

## Regras
- ATENÇÃO: follow-up vencido há 3+ dias, dor >= 7/10 ou risco >= 75.
- ALTA: follow-up vencido/hoje, feedback crítico ou risco >= 50.
- O radar não envia mensagens, não altera treinos e não toma decisões clínicas.
- Links de WhatsApp são apenas atalhos para ação explícita do Personal.
- A carteira continua limitada aos dados do Personal autenticado pelos serviços existentes.

## Fluxo
Sinal -> Radar Diário -> Prioridade -> CRM/WhatsApp/Perfil -> ação humana -> registro.
