package com.marceloaleixo.treinoflow.dto;

/**
 * Etapa 85: análise de tendência de desempenho usando várias sessões.
 * Somente recomendatória; nunca altera a prescrição do Personal.
 */
public record AnaliseTendenciaProgressaoView(
        String nivel,
        String confianca,
        String titulo,
        String resumo,
        String acao,
        int sessoesAnalisadas,
        int sessoesNoTopoFaixa,
        Integer repeticoesPrimeiraSessao,
        Integer repeticoesUltimaSessao,
        Double volumePrimeiraSessao,
        Double volumeUltimaSessao,
        Double variacaoVolumePercentual,
        Double rpeMedioPrimeiraSessao,
        Double rpeMedioUltimaSessao,
        Double variacaoRpe,
        Double scoreTendencia) {

    public boolean temDados() { return sessoesAnalisadas > 0; }
    public boolean temComparacao() { return sessoesAnalisadas >= 2; }
    public boolean altaConfianca() { return "ALTA".equals(confianca); }

    public String volumePrimeiroFormatado() { return volume(volumePrimeiraSessao); }
    public String volumeUltimoFormatado() { return volume(volumeUltimaSessao); }
    public String variacaoVolumeFormatada() {
        return variacaoVolumePercentual == null ? "—" : String.format("%+.1f%%", variacaoVolumePercentual);
    }
    public String variacaoRpeFormatada() {
        return variacaoRpe == null ? "—" : String.format("%+.1f", variacaoRpe);
    }
    public String scoreFormatado() {
        return scoreTendencia == null ? "—" : String.format("%.0f/100", scoreTendencia);
    }

    private String volume(Double valor) {
        if (valor == null) return "—";
        return String.format("%.0f kg·reps", valor);
    }
}
