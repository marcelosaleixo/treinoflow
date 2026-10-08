CREATE TABLE IF NOT EXISTS templates_notificacao (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(40) NOT NULL,
    canal VARCHAR(20) NOT NULL,
    titulo VARCHAR(140) NOT NULL,
    mensagem TEXT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_template_tipo_canal UNIQUE (tipo, canal)
);

CREATE INDEX IF NOT EXISTS idx_template_tipo ON templates_notificacao(tipo);

INSERT INTO templates_notificacao (tipo, canal, titulo, mensagem, ativo)
VALUES
('VENCIMENTO_7_DIAS','INTERNA','Sua cobrança vence em 7 dias','A cobrança de {{valor}} do plano {{plano}} vence em {{vencimento}}.',TRUE),
('VENCIMENTO_7_DIAS','EMAIL','Sua cobrança vence em 7 dias','Olá, {{nome}}! A cobrança de {{valor}} do plano {{plano}} vence em {{vencimento}}. {{link_pagamento}}',TRUE),
('VENCIMENTO_7_DIAS','WHATSAPP','Sua cobrança vence em 7 dias','Olá, {{nome}}! A cobrança de {{valor}} do plano {{plano}} vence em {{vencimento}}. {{link_pagamento}}',TRUE),
('VENCIMENTO_HOJE','INTERNA','Sua cobrança vence hoje','A cobrança de {{valor}} vence hoje. Acesse Cobranças para realizar o pagamento.',TRUE),
('VENCIMENTO_HOJE','EMAIL','Sua cobrança vence hoje','Olá, {{nome}}! Sua cobrança de {{valor}} vence hoje. Acesse o link para pagamento: {{link_pagamento}}',TRUE),
('VENCIMENTO_HOJE','WHATSAPP','Sua cobrança vence hoje','Olá, {{nome}}! Sua cobrança de {{valor}} vence hoje. Pagamento: {{link_pagamento}}',TRUE),
('COBRANCA_ATRASADA','INTERNA','Existe uma cobrança em atraso','A cobrança de {{valor}} venceu em {{vencimento}}. Regularize sua assinatura para evitar bloqueios.',TRUE),
('COBRANCA_ATRASADA','EMAIL','Existe uma cobrança em atraso','Olá, {{nome}}! A cobrança de {{valor}} venceu em {{vencimento}}. Regularize sua assinatura: {{link_pagamento}}',TRUE),
('COBRANCA_ATRASADA','WHATSAPP','Existe uma cobrança em atraso','Olá, {{nome}}! A cobrança de {{valor}} venceu em {{vencimento}}. Regularize sua assinatura: {{link_pagamento}}',TRUE),
('PAGAMENTO_CONFIRMADO','INTERNA','Pagamento confirmado','Recebemos o pagamento de {{valor}}. Sua assinatura foi atualizada.',TRUE),
('PAGAMENTO_CONFIRMADO','EMAIL','Pagamento confirmado','Olá, {{nome}}! Recebemos o pagamento de {{valor}} do plano {{plano}}. Sua assinatura foi atualizada.',TRUE),
('PAGAMENTO_CONFIRMADO','WHATSAPP','Pagamento confirmado','Olá, {{nome}}! Recebemos o pagamento de {{valor}} do plano {{plano}}. Sua assinatura foi atualizada.',TRUE),
('ASSINATURA_VENCENDO','INTERNA','Sua assinatura está próxima do vencimento','Seu plano {{plano}} está próximo do vencimento. Consulte sua assinatura para mais detalhes.',TRUE),
('ASSINATURA_VENCENDO','EMAIL','Sua assinatura está próxima do vencimento','Olá, {{nome}}! Seu plano {{plano}} está próximo do vencimento em {{vencimento}}. {{link_pagamento}}',TRUE),
('ASSINATURA_VENCENDO','WHATSAPP','Sua assinatura está próxima do vencimento','Olá, {{nome}}! Seu plano {{plano}} está próximo do vencimento em {{vencimento}}. {{link_pagamento}}',TRUE),
('ASSINATURA_BLOQUEADA','INTERNA','Sua assinatura foi bloqueada','Regularize sua assinatura para voltar a utilizar todos os recursos do TreinoFlow.',TRUE),
('ASSINATURA_BLOQUEADA','EMAIL','Sua assinatura foi bloqueada','Olá, {{nome}}! Sua assinatura do plano {{plano}} está bloqueada. Regularize sua situação para voltar a utilizar o TreinoFlow: {{link_pagamento}}',TRUE),
('ASSINATURA_BLOQUEADA','WHATSAPP','Sua assinatura foi bloqueada','Olá, {{nome}}! Sua assinatura do plano {{plano}} está bloqueada. Regularize sua situação: {{link_pagamento}}',TRUE)
ON CONFLICT (tipo, canal) DO NOTHING;
