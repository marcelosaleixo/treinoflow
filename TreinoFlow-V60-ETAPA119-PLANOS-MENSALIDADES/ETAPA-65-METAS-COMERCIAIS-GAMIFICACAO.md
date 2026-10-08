# ETAPA 65 — Metas Comerciais + Indicadores do Personal

## Objetivo
Criar uma visão mensal de gestão por metas, combinando carteira ativa, treinos concluídos e alunos recuperados.

## Rotas
- GET `/metas` — painel de metas do Personal.
- POST `/metas` — salva metas do mês atual.

## Indicadores
- Alunos ativos no mês atual.
- Treinos concluídos no mês atual.
- Recuperações/renovações distintas no mês atual.
- Progresso individual e progresso geral.
- Status: META BATIDA, ACELERANDO, NO CAMINHO ou PRECISA DE FOCO.

## Persistência
Migration V42 cria `metas_comerciais`, isolada por `personal_id` e `mes_referencia`.

## Observação
A receita individual dos alunos não foi incluída nesta etapa porque o modelo atual não possui cobrança/valor mensal vinculado a cada aluno. A etapa usa apenas indicadores efetivamente disponíveis no domínio atual.
