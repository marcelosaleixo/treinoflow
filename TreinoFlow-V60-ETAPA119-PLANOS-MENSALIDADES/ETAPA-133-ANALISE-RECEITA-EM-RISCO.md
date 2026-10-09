# Etapa 133 — Análise de receita em risco

## Objetivo
Cruzar o score operacional de risco de abandono com contas a receber pendentes/atrasadas associadas a cada aluno para ajudar o Personal a priorizar o acompanhamento.

## Entregas
- Nova tela `/financeiro/receita-em-risco`.
- Indicadores de exposição em aberto, valor atrasado, valor pendente, alunos com exposição e alunos com score >= 50 e exposição.
- Lista de alunos ativos do radar de retenção ordenada por score e exposição financeira.
- Exibição do principal sinal de risco e da ação de retenção sugerida pelo sistema.
- Atalho no dashboard financeiro e links para resumo por aluno e radar de retenção.
- Reutiliza tabelas, repositórios e serviço de risco existentes; não exige migração de banco.

## Regras e limites
- Somente contas associadas a aluno e pertencentes ao Personal autenticado entram no cálculo.
- Contas pagas e canceladas são excluídas da exposição.
- Exposição = valores em aberto pendentes + atrasados; não é previsão de perda nem lucro cessante.
- Score >= 50 é um critério operacional de prioridade, não uma probabilidade estatística.
- Nenhuma mensagem é enviada automaticamente.

## Validação
O arquivo ZIP deve ser verificado quanto à integridade. A compilação Maven e o teste de execução precisam ser realizados no ambiente do projeto antes do deploy.
