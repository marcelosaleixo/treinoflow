# ETAPA 118 — Dashboard Financeiro + Contas a Receber

## Objetivo
Criar o primeiro núcleo financeiro do Personal Trainer, separado das cobranças da assinatura do próprio SaaS TreinoFlow.

## O que entrou
- Dashboard financeiro em `/financeiro`.
- Receita recebida no mês.
- Previsão de recebimento no mês.
- Total a receber.
- Total em atraso e quantidade de contas atrasadas.
- Contas a receber vinculadas opcionalmente a um aluno.
- Cadastro manual de conta a receber.
- Registro de pagamento com forma de pagamento e data.
- Cancelamento de conta não paga.
- Atualização automática de contas vencidas de PENDENTE para ATRASADA ao abrir o financeiro.
- Proteção por personal autenticado: aluno e conta são sempre validados pelo `personal_id`.
- Layout responsivo seguindo o padrão visual atual.

## Separação importante
`Cobranca` / `Assinatura` continuam representando o faturamento da assinatura do Personal com o TreinoFlow.
`ContaReceber` representa o dinheiro que o Personal Trainer recebe dos próprios alunos.

## Próximas etapas financeiras
- ETAPA 119 — Planos/mensalidades dos alunos.
- ETAPA 120 — Histórico financeiro completo e filtros.
- ETAPA 121 — Despesas e categorias.
- ETAPA 122 — Recorrência automática.
- ETAPA 123 — Cobrança inteligente.
- ETAPA 124 — DRE simplificada.
