# ETAPA 105 — Inteligência Pós-Treino para o Personal

## Objetivo
Transformar o feedback estruturado do aluno em uma fila objetiva de ações para o Personal, cruzando os sinais do último treino com o score operacional de risco do aluno.

## Entregas
- Novo serviço `PosTreinoAcaoService`.
- Nova tela `/performance/pos-treino`.
- Priorização explicável: ATENÇÃO, ALTA, MÉDIA e BAIXA.
- Fila limitada aos 10 casos recentes com prioridade acima de BAIXA.
- Acesso direto ao Perfil Inteligente e ao CRM do aluno.
- Combinação de dor, esforço, energia, satisfação e risco de abandono.
- Nenhuma alteração automática de treino, carga ou prescrição.
- Sem nova tabela e sem nova migration: a etapa reutiliza os dados da Etapa 104 e o score de risco já existente.

## Regras principais
1. Dor >= 7/10 -> ATENÇÃO.
2. Risco >= 75 ou satisfação <= 2 + energia <= 2 -> ALTA.
3. Dor 4–6, esforço 5, energia <= 2 ou risco >= 50 -> MÉDIA.
4. Demais casos -> BAIXA, removida da fila principal.

## Segurança
O TreinoFlow apenas sugere a próxima ação. Não diagnostica dor e não altera prescrição automaticamente. O Personal continua responsável pela decisão e pode registrar a conduta no CRM.
