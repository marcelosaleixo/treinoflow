package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record HistoricoPerformanceDashboardView(
        String periodoLabel,
        PerformanceMensalView atual,
        PerformanceMensalView melhorMes,
        PerformanceMensalView evolucaoAnterior,
        int variacaoPontos,
        List<PerformanceMensalView> meses
) {
    public String getVariacaoPontosLabel() {
        if (variacaoPontos > 0) return "+" + variacaoPontos + " pts";
        if (variacaoPontos < 0) return variacaoPontos + " pts";
        return "0 pts";
    }
}
