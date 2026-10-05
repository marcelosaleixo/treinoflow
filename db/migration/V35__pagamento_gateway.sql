ALTER TABLE cobrancas_treinoflow ADD COLUMN IF NOT EXISTS gateway VARCHAR(30);
ALTER TABLE cobrancas_treinoflow ADD COLUMN IF NOT EXISTS external_payment_id VARCHAR(100);
ALTER TABLE cobrancas_treinoflow ADD COLUMN IF NOT EXISTS external_reference VARCHAR(120);
ALTER TABLE cobrancas_treinoflow ADD COLUMN IF NOT EXISTS qr_code TEXT;
ALTER TABLE cobrancas_treinoflow ADD COLUMN IF NOT EXISTS qr_code_base64 TEXT;
ALTER TABLE cobrancas_treinoflow ADD COLUMN IF NOT EXISTS payment_url TEXT;
CREATE INDEX IF NOT EXISTS idx_cobranca_external_payment_id ON cobrancas_treinoflow(external_payment_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_cobranca_external_reference ON cobrancas_treinoflow(external_reference) WHERE external_reference IS NOT NULL;
