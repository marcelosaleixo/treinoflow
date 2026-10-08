package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record OtimizacaoRetencaoDashboardView(
        String periodoLabel,
        long totalAcoes,
        long resultadosFinais,
        long sucessos,
        double taxaSucesso,
        List<EstrategiaView> estrategias,
        List<HorarioView> horarios,
        String resumo
) {
    public String taxaSucessoLabel() { return String.format(java.util.Locale.US, "%.1f%%", taxaSucesso); }

    public record EstrategiaView(
            String faixa,
            String acao,
            long amostra,
            long sucessos,
            double taxa,
            String confianca,
            String justificativa
    ) {
        public String taxaLabel() { return String.format(java.util.Locale.US, "%.1f%%", taxa); }
    }

    public record HorarioView(
            String faixa,
            int hora,
            long amostra,
            long sucessos,
            double taxa,
            String confianca
    ) {
        public String horaLabel() { return String.format("%02dh", hora); }
        public String taxaLabel() { return String.format(java.util.Locale.US, "%.1f%%", taxa); }
    }
}
