# ETAPA 67 — Desafios de Performance

## Objetivo
Adicionar desafios mensais de performance à gamificação do TreinoFlow, usando os indicadores reais já existentes no sistema.

## Rota
- `GET /gamificacao/desafios`

## Desafios
- Recuperador do mês: meta de recuperações/renovações da Meta Comercial.
- Máquina de consistência: meta de treinos concluídos da Meta Comercial.
- Carteira forte: meta de alunos ativos da Meta Comercial.
- Desafio triplo: levar o progresso geral das três metas principais a 100%.

## Pontuação bônus
Cada desafio possui bônus próprio e, ao atingir 100%, recebe `concluido_em`.

## Persistência
A tabela `desafios_performance` registra os desafios por Personal, mês e tipo. Não altera as tabelas existentes de alunos, treinos ou CRM.

## Segurança
A rota utiliza o Personal autenticado e todos os registros são filtrados pelo `personal_id`.

## Observação
O painel mostra o bônus dos desafios separadamente da pontuação-base da Etapa 66. Isso evita alterar retroativamente o cálculo do nível de gamificação já existente.
