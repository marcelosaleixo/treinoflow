# Etapa 135 — Plano de Ação Financeiro por Aluno

- Nova rota: `/financeiro/acoes-aluno`.
- Permite registrar contato preventivo, negociação/cobrança, renovação e revisão de plano.
- Salva ações usando a entidade `InteracaoCrm` existente e o serviço `CrmService`; não cria tabela nova.
- Permite abrir o histórico CRM de cada aluno a partir da mesma tela.
- Valida que a próxima ação não esteja no passado e exige data para resultado “Em acompanhamento”.
- O escopo é por Personal autenticado, validado por `CrmService`.
- Não envia WhatsApp nem efetua cobranças automaticamente.

Validação pendente: executar compilação Maven e teste funcional no ambiente local.
