# ETAPA 60 — Motor de Retenção: Score de Risco de Abandono

## Objetivo
Transformar os sinais já existentes no TreinoFlow em um score explicável de 0 a 100 para priorizar a carteira do Personal.

## Nova rota
- `/retencao/risco`

## Sinais utilizados
Todos são calculados com dados já existentes:
- faltas nos últimos 30 dias;
- queda de frequência comparando os últimos 15 dias com os 15 dias anteriores;
- dias desde o último treino realizado/concluído;
- média de satisfação registrada no Portal do Aluno;
- dias desde a última interação no CRM;
- queda de carga relevante entre as duas últimas execuções do mesmo exercício.

## Pontuação máxima
- Faltas: até 25 pontos
- Queda de frequência: até 15 pontos
- Inatividade: até 20 pontos
- Satisfação: até 15 pontos
- Falta de contato: até 10 pontos
- Queda de carga: até 15 pontos
- Total: 100 pontos

## Níveis
- CRITICO: 75–100
- ALTO: 50–74
- MEDIO: 25–49
- BAIXO: 0–24

## Ações sugeridas
O sistema mostra uma próxima ação recomendada de acordo com o nível. Nesta etapa não é criado um novo disparo automático de WhatsApp e não é criada nova tabela.

## Banco
Nenhuma migration foi necessária. A funcionalidade reutiliza `agendamentos`, `registros_treino_aluno`, `execucoes_exercicios`, `interacoes_crm` e `alunos`.
