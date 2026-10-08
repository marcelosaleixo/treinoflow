-- ETAPA 121 - Pagamentos divididos de uma conta a receber.
-- Permite quitar a mesma conta usando duas ou mais formas (ex.: R$ 100 em dinheiro + R$ 200 em PIX).
CREATE TABLE IF NOT EXISTS contas_receber_pagamentos (
    id BIGSERIAL PRIMARY KEY,
    conta_receber_id BIGINT NOT NULL REFERENCES contas_receber_personal(id) ON DELETE CASCADE,
    forma_pagamento VARCHAR(30) NOT NULL,
    valor NUMERIC(12,2) NOT NULL,
    data_pagamento DATE NOT NULL,
    observacao VARCHAR(300)
);
CREATE INDEX IF NOT EXISTS idx_pagamento_conta_receber ON contas_receber_pagamentos(conta_receber_id);
CREATE INDEX IF NOT EXISTS idx_pagamento_forma ON contas_receber_pagamentos(forma_pagamento);
