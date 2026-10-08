package com.marceloaleixo.treinoflow.dto;

public record MesMetaRetencaoView(
        String mesLabel,
        int metaRecuperacoes,
        long recuperados,
        double metaTaxa,
        double taxaAtual,
        int atingimento
) {
    public String getTaxaAtualLabel() { return String.format(java.util.Locale.US, "%.1f%%", taxaAtual); }
    public String getMetaTaxaLabel() { return String.format(java.util.Locale.US, "%.1f%%", metaTaxa); }
    public String getAtingimentoStyle() { return "width:" + atingimento + "%"; }
}
