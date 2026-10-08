# Etapa 42 — CRM e Recuperação de Alunos

## Objetivo
Transformar o TreinoFlow em uma ferramenta de relacionamento para o Personal Trainer, permitindo registrar contatos, criar follow-ups e identificar alunos ativos sem acompanhamento recente.

## Funcionalidades
- Dashboard `/crm`.
- Timeline individual `/crm/alunos/{id}`.
- Registro de interações por WhatsApp, e-mail, telefone ou interno.
- Tipos: contato, retenção, renovação, pós-venda, cobrança e outro.
- Resultados: sem resposta, em acompanhamento, recuperado, renovado, cancelamento e outro.
- Próxima ação com data.
- Indicador de risco de churn: aluno ativo sem interação CRM nos últimos 30 dias.
- Taxa de recuperação: recuperados / (recuperados + cancelamentos).
- Contadores de alunos ativos, inativos, recuperados, renovados e cancelamentos.
- Ações rápidas para abrir WhatsApp, e-mail e telefone quando os dados estiverem disponíveis.
- Histórico completo por aluno.

## Modelagem
Tabela `interacoes_crm` vinculada ao Personal e ao Aluno.

## Migration
`V40__crm_alunos_retencao.sql`.

## Observação
A Etapa 42 não inventa um valor financeiro recuperado porque o modelo de negócio atual não possui cobrança financeira vinculada aos alunos do Personal; as cobranças existentes pertencem à assinatura do próprio Personal no TreinoFlow. O painel usa métricas de relacionamento e retenção que o modelo atual suporta.
