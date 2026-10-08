# ETAPA 96 — ATENÇÃO HOJE

## Objetivo
Transformar o risco de abandono em uma fila operacional no dashboard do Personal.

## Implementação
- Dashboard calcula o risco dos alunos ativos usando `ScoreRiscoAlunoService`.
- Exibe até 5 alunos com score >= 25, ordenados pelo risco.
- Exibe contagem de alunos críticos (>=75) e altos (50–74).
- Link direto para o Perfil Inteligente do aluno.
- Quando não há sinais moderados ou maiores, mostra estado positivo.
- Não envia mensagens nem altera treinos automaticamente.

## Fluxo
`Risco → Atenção Hoje → Perfil Inteligente → decisão do Personal`
