package com.marceloaleixo.treinoflow.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class WhatsAppNotificacaoService {
    private final RestClient client;
    private final boolean enabled;
    private final String apiKey;
    private final String endpoint;
    private final String instance;

    public WhatsAppNotificacaoService(
            @Value("${treinoflow.notificacoes.whatsapp.enabled:false}") boolean enabled,
            @Value("${treinoflow.notificacoes.whatsapp.base-url:}") String baseUrl,
            @Value("${treinoflow.notificacoes.whatsapp.api-key:}") String apiKey,
            @Value("${treinoflow.notificacoes.whatsapp.endpoint:/message/sendText/{instance}}") String endpoint,
            @Value("${treinoflow.notificacoes.whatsapp.instance:}") String instance) {
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.instance = instance;
        this.client = RestClient.builder().baseUrl(baseUrl == null ? "" : baseUrl).build();
    }

    public void enviar(String numero, String mensagem) {
        if (!enabled) throw new IllegalStateException("Envio de WhatsApp está desativado.");
        if (numero == null || numero.isBlank()) throw new IllegalArgumentException("Telefone do destinatário não informado.");
        if (apiKey == null || apiKey.isBlank()) throw new IllegalStateException("API key do WhatsApp não configurada.");
        if (instance == null || instance.isBlank()) throw new IllegalStateException("Instância do WhatsApp não configurada.");
        String path = endpoint.replace("{instance}", instance);
        client.post().uri(path)
                .header("apikey", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("number", normalizar(numero), "text", mensagem))
                .retrieve().toBodilessEntity();
    }

    private String normalizar(String numero) {
        String digits = numero.replaceAll("\\D", "");
        if (digits.startsWith("00")) digits = digits.substring(2);
        if (digits.length() == 11) digits = "55" + digits;
        return digits;
    }
}
