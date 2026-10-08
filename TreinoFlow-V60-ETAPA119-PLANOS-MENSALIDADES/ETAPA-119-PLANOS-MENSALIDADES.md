# ETAPA 119 — Planos e Mensalidades

Objetivo: permitir que o Personal cadastre uma mensalidade recorrente por aluno e gere a conta a receber do ciclo sem duplicar a mesma cobrança.

## Entregas
- Cadastro de plano por aluno.
- Valor mensal e dia fixo de vencimento (1 a 28).
- Data de início e observação.
- Ativar/pausar plano.
- Gerar cobrança do ciclo atual.
- Vínculo entre cobrança e plano mensal.
- Proteção por personal_id em todas as operações.
- Navegação Financeiro → Planos e mensalidades → Contas a receber.

## Regra de segurança
O dia é limitado a 28 para evitar vencimentos inválidos em fevereiro e meses menores. A geração de cobrança verifica se já existe uma conta para o plano naquele vencimento.

## Fora desta etapa
Recorrência automática mensal, gateway, PIX, cobrança por WhatsApp, despesas e DRE. Essas funções permanecem para etapas posteriores.
