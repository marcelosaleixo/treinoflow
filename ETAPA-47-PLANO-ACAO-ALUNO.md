# ETAPA 47 — Plano de Ação do Aluno

## Objetivo
Transformar os alertas da Etapa 46 em uma fila operacional para o Personal agir sobre alunos que apresentam sinais de baixa frequência, inatividade ou baixa satisfação.

## Funcionalidades
- Nova rota `/performance/acoes`.
- Lista somente alunos ativos do Personal autenticado.
- Classificação por prioridade ALTA/MEDIA.
- Motivo objetivo do alerta.
- Ação recomendada para cada aluno.
- Indicadores de sessões em 7 e 30 dias e média de nota.
- Atalho para o CRM do aluno.
- Atalho para a evolução do aluno.
- Atalho para WhatsApp quando houver telefone cadastrado.
- Resumo da quantidade de ações por prioridade.
- Link do Dashboard de Performance para o Plano de Ação.

## Regras
1. Sem treino nos últimos 30 dias: RETENCAO / ALTA.
2. Sem treino nos últimos 7 dias, mas com atividade nos últimos 30 dias: FREQUENCIA / ALTA.
3. Menos de 5 sessões nos últimos 30 dias: ADESAO / MEDIA.
4. Média de avaliação abaixo de 3,5: SATISFACAO / MEDIA.
5. Um aluno pode aparecer somente uma vez; é aplicada a primeira regra de maior prioridade que se encaixar.

## Segurança
Todos os alunos são filtrados pelo `personalId` do usuário autenticado. Não existe rota que aceite `personalId` fornecido pelo navegador.

## Persistência
Esta etapa não cria tabelas nem migrations. As recomendações são calculadas com os registros existentes.
