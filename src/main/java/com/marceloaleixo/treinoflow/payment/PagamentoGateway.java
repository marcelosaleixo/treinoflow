package com.marceloaleixo.treinoflow.payment;

import com.marceloaleixo.treinoflow.entity.Cobranca;

public interface PagamentoGateway {
    ResultadoPix criarPix(Cobranca cobranca);
    void processarWebhook(String externalId);

    record ResultadoPix(String externalId, String externalReference, String qrCode,
                        String qrCodeBase64, String paymentUrl) {}
}
