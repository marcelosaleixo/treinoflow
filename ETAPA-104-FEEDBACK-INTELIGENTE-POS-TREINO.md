# ETAPA 104 — Feedback Inteligente Pós-Treino

## Objetivo
Transformar o encerramento do treino em um feedback estruturado para o Personal, sem automatizar decisões de prescrição.

## Fluxo do aluno
- Satisfação do treino: 1–5.
- Esforço percebido: 1–5.
- Dor/desconforto: 0–10.
- Energia: 1–5.
- Observação livre.

## Inteligência
O TreinoFlow interpreta os sinais registrados e classifica o feedback como `POSITIVO`, `ESTÁVEL`, `ACOMPANHAR` ou `ATENÇÃO`. Dor elevada, esforço muito alto, energia baixa e satisfação baixa geram sinais específicos e ações sugeridas para acompanhamento.

## Segurança
A interpretação é operacional e não diagnóstica. O sistema não altera carga, treino ou prescrição automaticamente com base no feedback. O Personal decide a ação.

## Persistência
Foram adicionados os campos opcionais `esforco`, `dor` e `energia` em `registros_treino_aluno`, com validações no backend e migration `V47__feedback_inteligente_treino.sql`. A satisfação continua usando o campo `nota` existente.

## Painel
O dashboard `/performance` passa a exibir os 10 feedbacks estruturados mais recentes da carteira, com sinais, status, resumo e acesso ao perfil do aluno.
