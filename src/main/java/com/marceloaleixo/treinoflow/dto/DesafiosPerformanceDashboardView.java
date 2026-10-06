package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record DesafiosPerformanceDashboardView(
        String mesLabel,
        List<DesafioPerformanceView> desafios,
        int concluidos,
        int total,
        int bonusConquistados,
        int progressoGeral,
        String nivel,
        int pontosGamificacao
) {
    public String getProgressoGeralLabel() { return progressoGeral + "%"; }
}
