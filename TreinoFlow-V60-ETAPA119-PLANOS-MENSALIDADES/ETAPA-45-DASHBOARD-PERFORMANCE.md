# ETAPA 45 — Dashboard de Performance

## Objetivo
Criar uma visão operacional para o Personal acompanhar frequência, consistência e alunos que precisam de atenção.

## Entregas
- Novo endpoint `GET /performance`.
- Novo `DashboardPerformanceService` isolado por Personal.
- Indicadores de sessões concluídas nos últimos 7 e 30 dias.
- Média das notas dos treinos registrados.
- Quantidade de alunos ativos com e sem atividade nos últimos 30 dias.
- Histórico visual das últimas 8 semanas.
- Lista de alunos que precisam de atenção.
- Lista dos alunos mais ativos.
- Link de acesso no dashboard principal.

## Regras
- Apenas alunos ativos do Personal autenticado entram na carteira de performance.
- Só registros marcados como concluídos contam como sessão.
- Um aluno é considerado sem atividade de 30 dias quando não possui sessão concluída nesse período.
- A tela não inventa carga ou evolução numérica: a etapa usa os registros reais de sessões e notas já armazenados.

## Observação técnica
O histórico do Personal é carregado com `JOIN FETCH` para evitar problemas de LazyInitialization, pois o projeto utiliza `spring.jpa.open-in-view=false`.
