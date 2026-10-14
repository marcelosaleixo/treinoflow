-- CORREÇÃO DEFINITIVA: tipos permitidos em notificacoes_treinoflow
-- Execute este script no PostgreSQL do banco utilizado pela aplicação TreinoFlow.

BEGIN;

ALTER TABLE public.notificacoes_treinoflow
    DROP CONSTRAINT IF EXISTS notificacoes_treinoflow_tipo_check;

ALTER TABLE public.notificacoes_treinoflow
    ADD CONSTRAINT notificacoes_treinoflow_tipo_check
    CHECK (tipo IN (
        'VENCIMENTO_7_DIAS',
        'VENCIMENTO_HOJE',
        'COBRANCA_ATRASADA',
        'PAGAMENTO_CONFIRMADO',
        'ASSINATURA_VENCENDO',
        'ASSINATURA_BLOQUEADA',
        'INADIMPLENCIA_3_DIAS',
        'INADIMPLENCIA_7_DIAS',
        'META_RECEITA_ATINGIDA',
        'META_RECEITA_ABAIXO_RITMO',
        'META_RECEITA_NAO_DEFINIDA',
        'META_RECEITA_MES_ENCERRADO'
    ));

COMMIT;
