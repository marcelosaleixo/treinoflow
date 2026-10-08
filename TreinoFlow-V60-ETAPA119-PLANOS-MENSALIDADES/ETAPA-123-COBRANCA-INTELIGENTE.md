# Etapa 123 — Cobrança Inteligente

## Objetivo
Transformar o contas a receber em uma fila operacional que mostra ao Personal quem precisa de lembrete, cobrança ou acompanhamento, conectando o Financeiro ao CRM.

## Regras
- Contas `PENDENTE` e `ATRASADA` com vencimento até 7 dias no futuro entram na fila.
- Contas vencidas são classificadas por atraso:
  - 1 a 3 dias: ALTA
  - 4 dias ou mais: CRÍTICA
- Vencimento hoje: ALTA.
- Vencimento em até 3 dias: MÉDIA.
- Vencimento entre 4 e 7 dias: BAIXA.
- O sistema sugere mensagem e ação, mas não envia automaticamente.
- WhatsApp abre a conversa com a mensagem sugerida.
- Registrar cobrança cria uma interação `COBRANCA` no CRM.
- Se continuar em acompanhamento, a próxima ação é obrigatória.
- Uma cobrança em acompanhamento não pode ser duplicada para a mesma conta.
- Registrar cobrança não marca a conta como paga. A baixa continua sendo feita no Financeiro.
- Isolamento por Personal é mantido em todas as consultas e ações.

## Rotas
- `GET /financeiro/cobranca-inteligente`
- `GET /financeiro/cobranca-inteligente/conta/{contaId}`
- `POST /financeiro/cobranca-inteligente/conta/{contaId}/registrar`

## Banco
Não foi criada tabela nova. A etapa utiliza `contas_receber_personal` e `interacoes_crm` existentes.
