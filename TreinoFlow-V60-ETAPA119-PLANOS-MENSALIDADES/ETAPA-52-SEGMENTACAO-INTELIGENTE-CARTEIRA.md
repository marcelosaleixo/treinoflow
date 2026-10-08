# ETAPA 52 — Segmentação Inteligente da Carteira

## Objetivo
Transformar os sinais de treino, satisfação e contato em uma visão operacional da carteira do Personal.

## Nova tela
- `GET /retencao/carteira`

## Segmentos
- `SAUDAVEL`: risco 0–24
- `ATENCAO`: risco 25–49
- `RISCO`: risco 50–74
- `CRITICO`: risco 75–100
- `RECUPERADO`: aluno com resultado `RECUPERADO` nos últimos 30 dias

## Pontuação
- +50: sem treino há 30 dias ou mais
- +20: sem treino na última semana, quando houve treino nos últimos 30 dias
- +15: menos de 5 sessões nos últimos 30 dias
- +15: média de avaliação abaixo de 3,5
- +10: sem contato CRM nos últimos 30 dias
- limite máximo: 100

A pontuação é um indicador operacional de priorização e não uma previsão clínica ou estatística de churn.

## Segurança
Todos os dados são filtrados pelo `personalId` do usuário autenticado.
