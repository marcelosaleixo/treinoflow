# ETAPA 68 — Histórico de Performance e Evolução do Personal

## Objetivo
Criar uma visão histórica de seis meses para acompanhar pontos, nível, metas, treinos, recuperações, desafios e evolução mensal do Personal.

## Rota
- GET `/gamificacao/historico`

## Funcionalidades
- Histórico dos últimos seis meses.
- Pontuação mensal calculada a partir das metas, treinos, recuperações, alunos acompanhados e bônus de desafios disponíveis no período.
- Nível mensal: Iniciante, Em evolução, Alta performance ou Elite.
- Identificação do melhor mês.
- Comparação do mês atual com o mês anterior.
- Progresso médio das metas.
- Quantidade de desafios concluídos e bônus conquistados.

## Regra de histórico da carteira
O domínio atual não possui uma tabela de histórico de alterações do status do aluno. Para não transformar o status atual em um falso histórico, a coluna histórica usa `alunos acompanhados`: alunos distintos com pelo menos um treino concluído no mês.

## Banco de dados
Não foi criada nova tabela nem migration. A etapa reutiliza `metas_comerciais`, `desafios_performance`, `registros_treino_aluno` e `interacoes_crm`.

## Isolamento
Todas as consultas são filtradas pelo `personalId` autenticado.
