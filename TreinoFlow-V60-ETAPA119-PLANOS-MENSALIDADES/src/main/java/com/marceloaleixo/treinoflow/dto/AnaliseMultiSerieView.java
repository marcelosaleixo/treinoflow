package com.marceloaleixo.treinoflow.dto;

/**
 * Análise da execução por múltiplas séries e sessões. É apenas informativa:
 * não altera a prescrição definida pelo Personal.
 */
public record AnaliseMultiSerieView(
        String nivel,
        String titulo,
        String resumo,
        String detalhe,
        int sessoesAnalisadas,
        int seriesUltimaSessao,
        int repeticoesUltimaSessao,
        Double volumeUltimaSessao,
        Double volumeSessaoAnterior,
        Double variacaoVolumePercentual,
        Double rpeMedioUltimaSessao,
        Double rpeMedioPrimeiraSerie,
        Double rpeMedioUltimaSerie) {

    public boolean temHistorico() {
        return sessoesAnalisadas > 0;
    }

    public boolean temComparacao() {
        return sessoesAnalisadas >= 2;
    }

    public String volumeUltimaSessaoFormatado() {
        return formatar(volumeUltimaSessao);
    }

    public String volumeSessaoAnteriorFormatado() {
        return formatar(volumeSessaoAnterior);
    }

    public String variacaoVolumeFormatada() {
        if (variacaoVolumePercentual == null) return "—";
        return String.format("%+.1f%%", variacaoVolumePercentual);
    }

    private String formatar(Double valor) {
        if (valor == null) return "—";
        return String.format("%.0f kg·reps", valor);
    }
}
