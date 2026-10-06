package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record MetaRetencaoDashboardView(
        String mesLabel,
        int metaRecuperacoes,
        long recuperados,
        double metaTaxa,
        double taxaAtual,
        int percentualMetaRecuperacoes,
        int percentualMetaTaxa,
        List<MesMetaRetencaoView> historico
) {
    public String getTaxaAtualLabel() { return String.format(java.util.Locale.US, "%.1f%%", taxaAtual); }
    public String getMetaTaxaLabel() { return String.format(java.util.Locale.US, "%.1f%%", metaTaxa); }
    public String getAtingimentoRecuperacoesLabel() { return percentualMetaRecuperacoes + "%"; }
    public String getAtingimentoTaxaLabel() { return percentualMetaTaxa + "%"; }
    public boolean isMetaRecuperacoesAtingida() { return recuperados >= metaRecuperacoes; }
    public boolean isMetaTaxaAtingida() { return taxaAtual >= metaTaxa; }
}
