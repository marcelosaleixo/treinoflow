# Etapa 53 — Ações Inteligentes + Checklist de Exercícios

## 1. Ações Inteligentes por Segmento

Nova tela `/retencao/acoes-inteligentes` transforma a segmentação da carteira em uma recomendação operacional.

- CRITICO: entrar em contato hoje.
- RISCO: fazer contato de recuperação.
- ATENCAO: acompanhar nesta semana.
- RECUPERADO: fidelizar retorno.
- SAUDAVEL: manter acompanhamento.

A tela exibe prioridade, score, motivos, ação sugerida e mensagem de WhatsApp personalizada. O sistema apenas prepara a mensagem; o envio continua sob decisão do Personal.

## 2. Checklist na ficha do treino do aluno

A ficha do Portal do Aluno passou a ter um checkbox em cada exercício: `✓ Exercício concluído`.

O checkbox utiliza a coluna existente `concluido` da entidade `ExecucaoExercicio`, portanto não foi criada uma tabela paralela.

Ao salvar o progresso:
- checkbox marcado = exercício concluído;
- checkbox desmarcado = exercício não concluído;
- carga, repetições e observação continuam sendo registradas independentemente do checkbox;
- ao reabrir o treino no mesmo dia, os exercícios já marcados permanecem marcados.

O botão foi alterado para `Salvar progresso do treino`, deixando claro que o aluno pode registrar o andamento da sessão.

## 3. Banco de dados

Não é necessária nova migration: `execucoes_exercicios.concluido` já existe desde a Etapa 44 e é reutilizada nesta etapa.
