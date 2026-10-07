# Etapa 82 — Execução de Treino Interativa

## Objetivo
Transformar o portal do aluno em uma experiência de execução de treino, com registro individual de séries, RPE, histórico da última sessão e contador de descanso configurado pelo Personal.

## Entregas
- Registro de cada série em `execucoes_series`.
- Carga, repetições, observação e RPE de 1 a 10 por série.
- Histórico da última série executada para o exercício.
- Botão para reutilizar carga/repetições anteriores como referência.
- Contador de descanso usando `TreinoExercicio.descansoSegundos` já configurado pelo Personal.
- Ao registrar uma série, a quantidade de séries restantes é atualizada imediatamente.
- Durante o descanso, o contador informa quando a próxima série estará liberada.
- Botões `+30s` e `Pular descanso`.
- Progresso global do treino.
- Navegação anterior/próximo exercício.
- Feedback final do treino.
- Compatibilidade preservada com `ExecucaoExercicio`, usado pelas etapas anteriores.

## Regra do contador
O contador não marca uma série como concluída sozinho. A série é considerada concluída quando o aluno registra carga/repetições. O descanso começa depois desse registro e, ao zerar, apenas libera/foca a próxima série. Isso evita contabilizar uma série que o aluno não executou.

## Banco
Migration: `V51__execucao_series_e_rpe.sql`.

## Endpoint
`POST /portal/{token}/treinos/{treinoId}/exercicios/{treinoExercicioId}/series`

O endpoint registra uma série sem recarregar a página.
