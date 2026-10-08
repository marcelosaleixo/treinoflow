# ETAPA 43 — Portal do Aluno

## Objetivo
Criar uma experiência simples e responsiva para o aluno acessar os treinos liberados sem precisar de uma conta de Personal.

## Funcionalidades
- Portal individual por token seguro.
- Lista de treinos liberados e não expirados.
- Visualização detalhada dos exercícios, séries, repetições, carga, descanso, observações e vídeo.
- Registro de treino concluído.
- Nota de 1 a 5 e feedback do aluno.
- Histórico das últimas atividades.
- Acesso do Personal pelo botão Portal na lista de alunos.

## Segurança
- O token é aleatório (UUID sem hífens) e exclusivo por aluno.
- Apenas alunos ATIVOS conseguem abrir o portal.
- O portal não expõe telas administrativas.
- Apenas treinos LIBERADOS e ainda válidos aparecem.

## Migration
`V41__portal_aluno_registros.sql` cria `alunos.token_portal` e `registros_treino_aluno`.

## Fluxo
Personal → Alunos → Portal → link individual → aluno abre → escolhe treino → executa → registra conclusão/feedback → Personal poderá acompanhar os registros nas próximas etapas.
