package com.marceloaleixo.treinoflow.dto;

/** Etapa 87 - sugestão de prescrição assistida. Somente recomendatória. */
public record PrescricaoAssistidaView(
        String nivel,
        String confianca,
        String titulo,
        String justificativa,
        String seriesSugeridas,
        String repeticoesSugeridas,
        String cargaSugerida,
        String acaoPersonal) {

    public boolean altaConfianca() { return "ALTA".equals(confianca); }
}
