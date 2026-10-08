INSERT INTO templates_notificacao (tipo, canal, titulo, mensagem, ativo)
VALUES
('INADIMPLENCIA_3_DIAS','INTERNA','Sua cobrança está em atraso há 3 dias','A cobrança de {{valor}} venceu em {{vencimento}}. Regularize agora para evitar a suspensão da assinatura.',TRUE),
('INADIMPLENCIA_3_DIAS','EMAIL','Sua cobrança está em atraso há 3 dias','Olá, {{nome}}! A cobrança de {{valor}} venceu em {{vencimento}} e está em atraso há 3 dias. Regularize sua assinatura: {{link_pagamento}}',TRUE),
('INADIMPLENCIA_3_DIAS','WHATSAPP','Sua cobrança está em atraso há 3 dias','Olá, {{nome}}! A cobrança de {{valor}} está em atraso há 3 dias. Regularize sua assinatura: {{link_pagamento}}',TRUE),
('INADIMPLENCIA_7_DIAS','INTERNA','Último aviso: cobrança em atraso há 7 dias','A cobrança de {{valor}} está em atraso há 7 dias. Regularize sua assinatura para evitar a suspensão dos serviços.',TRUE),
('INADIMPLENCIA_7_DIAS','EMAIL','Último aviso: cobrança em atraso há 7 dias','Olá, {{nome}}! A cobrança de {{valor}} está em atraso há 7 dias. Regularize sua assinatura para evitar a suspensão dos serviços: {{link_pagamento}}',TRUE),
('INADIMPLENCIA_7_DIAS','WHATSAPP','Último aviso: cobrança em atraso há 7 dias','Olá, {{nome}}! A cobrança de {{valor}} está em atraso há 7 dias. Regularize sua assinatura para evitar a suspensão dos serviços: {{link_pagamento}}',TRUE)
ON CONFLICT (tipo, canal) DO NOTHING;
