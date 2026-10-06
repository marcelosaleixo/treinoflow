package com.marceloaleixo.treinoflow.dto;

public record RetencaoResultadosDashboardView(
        long acoes,
        long contatos,
        long respostas,
        long recuperados,
        long cancelamentos,
        long emAcompanhamento,
        double taxaRecuperacao
) {
    public String getTaxaRecuperacaoLabel() {
        return String.format(java.util.Locale.US, "%.1f%%", taxaRecuperacao);
    }
}
