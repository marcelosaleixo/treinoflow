package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record TendenciaPerformanceDashboardView(
        String tendencia,
        String tendenciaDescricao,
        int variacaoMediaPontos,
        int projecaoProximoMes,
        String projecaoNivel,
        int mediaPontos,
        String alerta,
        String recomendacao,
        PerformanceMensalView atual,
        List<PerformanceMensalView> meses
) {
    public String getVariacaoMediaLabel() {
        if (variacaoMediaPontos > 0) return "+" + variacaoMediaPontos + " pts/mês";
        if (variacaoMediaPontos < 0) return variacaoMediaPontos + " pts/mês";
        return "Estável";
    }

    public String getProjecaoLabel() { return projecaoProximoMes + " pontos"; }
}
