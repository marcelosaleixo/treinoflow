# ETAPA 63 — Painel Executivo de Retenção

## Objetivo
Consolidar em uma única visão a saúde da carteira, o nível de risco dos alunos e os resultados das ações de retenção.

## Rota
- GET `/retencao/executivo`

## Indicadores
- Alunos ativos
- Alunos em risco
- Percentual da carteira em risco
- Recuperados ou renovados
- Taxa de recuperação
- Alunos sem contato há 30 dias
- Follow-ups pendentes
- Cancelamentos
- Distribuição por nível de risco

## Regra
O painel reutiliza o Score de Risco e o CRM existentes. Não cria tabela nem migration nova.

## Fluxo
Score de risco → Central de recuperação → Resultado da ação → Painel Executivo.
