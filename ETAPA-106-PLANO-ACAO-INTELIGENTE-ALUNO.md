# ETAPA 106 — Plano de Ação Inteligente por Aluno

## Objetivo
Transformar os sinais já existentes no TreinoFlow em uma fila operacional por aluno, cruzando risco de abandono e feedback pós-treino para responder: **“o que preciso fazer agora?”**.

## Implementação
- Nova rota: `/performance/plano-inteligente`.
- Novo `PlanoAcaoInteligenteService`.
- Novo `PlanoAcaoInteligenteView`.
- Nova tela `performance/plano-inteligente.html`.
- Integração do link no Dashboard de Performance.
- Combinação de `ScoreRiscoAlunoService` + `FeedbackTreinoInteligenteService`.
- Priorização: ATENÇÃO, ALTA, MÉDIA e BAIXA.
- Plano em quatro passos para cada aluno priorizado.
- Motivo principal e sinais observáveis.
- Resumo do último feedback quando disponível.
- Atalhos para CRM, Perfil Inteligente, Treinos e WhatsApp com mensagem sugerida.

## Regras de decisão
1. Dor/desconforto >= 7 no último feedback: ATENÇÃO.
2. Feedback em ATENÇÃO, risco >= 75 ou satisfação <= 2 + energia <= 2: ALTA.
3. Sinais relevantes no feedback ou risco >= 50: MÉDIA.
4. Risco >= 25: BAIXA.
5. Sem sinal acionável: o aluno não aparece na fila.

## Segurança
- O `personalId` vem do usuário autenticado.
- O serviço considera somente alunos ativos daquele Personal.
- Nenhum ajuste de carga, exercício, série ou prescrição é realizado automaticamente.
- Dor é tratada como sinal de acompanhamento, não como diagnóstico.
- O envio de WhatsApp continua sendo uma ação explícita do Personal.

## Persistência
Não foi criada nova tabela nem migration. A etapa reutiliza os dados existentes de risco, execução e feedback.
