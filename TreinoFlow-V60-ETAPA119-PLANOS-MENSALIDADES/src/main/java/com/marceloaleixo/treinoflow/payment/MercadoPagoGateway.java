package com.marceloaleixo.treinoflow.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marceloaleixo.treinoflow.entity.Cobranca;
import com.marceloaleixo.treinoflow.enums.StatusCobranca;
import com.marceloaleixo.treinoflow.service.CobrancaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MercadoPagoGateway implements PagamentoGateway {
    private final RestClient client;
    private final ObjectMapper mapper;
    private final CobrancaService cobrancas;
    private final String accessToken;
    private final String notificationUrl;

    public MercadoPagoGateway(ObjectMapper mapper, CobrancaService cobrancas,
                              @Value("${treinoflow.pagamento.mercadopago.access-token:}") String accessToken,
                              @Value("${treinoflow.pagamento.mercadopago.notification-url:}") String notificationUrl) {
        this.mapper = mapper;
        this.cobrancas = cobrancas;
        this.accessToken = accessToken;
        this.notificationUrl = notificationUrl;
        this.client = RestClient.builder().baseUrl("https://api.mercadopago.com").build();
    }

    @Override
    public ResultadoPix criarPix(Cobranca cobranca) {
        validarConfiguracao();
        if (cobranca.getExternalPaymentId() != null && !cobranca.getExternalPaymentId().isBlank()) {
            return new ResultadoPix(cobranca.getExternalPaymentId(), cobranca.getExternalReference(),
                    cobranca.getQrCode(), cobranca.getQrCodeBase64(), cobranca.getPaymentUrl());
        }

        String externalReference = "TF-COBRANCA-" + cobranca.getId();
        Map<String, Object> payment = new LinkedHashMap<>();
        payment.put("amount", cobranca.getValor().toPlainString());
        payment.put("payment_method", Map.of("id", "pix", "type", "bank_transfer"));
        payment.put("expiration_time", "P3D");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "online");
        body.put("total_amount", cobranca.getValor().toPlainString());
        body.put("external_reference", externalReference);
        body.put("processing_mode", "automatic");
        body.put("notification_url", notificationUrl);
        body.put("transactions", Map.of("payments", java.util.List.of(payment)));
        body.put("payer", Map.of("email", cobranca.getAssinatura().getPersonal().getEmail()));

        String json = client.post()
                .uri("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .header("X-Idempotency-Key", "cobranca-" + cobranca.getId())
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = mapper.readTree(json);
            JsonNode p = root.path("transactions").path("payments").path(0);
            String externalId = p.path("id").asText(null);
            String qr = p.path("payment_method").path("qr_code").asText(null);
            String qrBase64 = p.path("payment_method").path("qr_code_base64").asText(null);
            String ticket = p.path("payment_method").path("ticket_url").asText(null);
            cobrancas.registrarPagamentoGateway(cobranca.getId(), "MERCADO_PAGO", externalId,
                    externalReference, qr, qrBase64, ticket);
            return new ResultadoPix(externalId, externalReference, qr, qrBase64, ticket);
        } catch (Exception e) {
            throw new IllegalStateException("Resposta inválida do Mercado Pago ao criar o PIX.", e);
        }
    }

    @Override
    public void processarWebhook(String externalId) {
        validarConfiguracao();
        String json = client.get()
                .uri("/v1/orders/{id}", externalId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(String.class);
        try {
            JsonNode root = mapper.readTree(json);
            String reference = root.path("external_reference").asText(null);
            String status = root.path("status").asText("");
            JsonNode payment = root.path("transactions").path("payments").path(0);
            String paymentStatus = payment.path("status").asText("");
            if ("processed".equalsIgnoreCase(status) || "approved".equalsIgnoreCase(paymentStatus)) {
                if (reference != null && reference.startsWith("TF-COBRANCA-")) {
                    Long cobrancaId = Long.valueOf(reference.substring("TF-COBRANCA-".length()));
                    cobrancas.marcarPagaPorGateway(cobrancaId, externalId);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível processar o pedido do Mercado Pago.", e);
        }
    }

    public boolean validarAssinaturaWebhook(String signature, String requestId, String dataId,
                                            String secret) {
        if (signature == null || requestId == null || dataId == null || secret == null || secret.isBlank()) return false;
        String ts = null, v1 = null;
        for (String part : signature.split(",")) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2 && "ts".equals(kv[0])) ts = kv[1];
            if (kv.length == 2 && "v1".equals(kv[0])) v1 = kv[1];
        }
        if (ts == null || v1 == null) return false;
        String manifest = "id:" + dataId.toLowerCase() + ";request-id:" + requestId + ";ts:" + ts + ";";
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String calculated = HexFormat.of().formatHex(mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8)));
            return java.security.MessageDigest.isEqual(calculated.getBytes(StandardCharsets.US_ASCII), v1.getBytes(StandardCharsets.US_ASCII));
        } catch (Exception e) {
            return false;
        }
    }

    private void validarConfiguracao() {
        if (accessToken == null || accessToken.isBlank()) throw new IllegalStateException("Mercado Pago não configurado: defina TREINOFLOW_MP_ACCESS_TOKEN.");
        if (notificationUrl == null || notificationUrl.isBlank() || !notificationUrl.startsWith("https://")) {
            throw new IllegalStateException("Mercado Pago exige TREINOFLOW_MP_NOTIFICATION_URL com HTTPS público.");
        }
    }
}
