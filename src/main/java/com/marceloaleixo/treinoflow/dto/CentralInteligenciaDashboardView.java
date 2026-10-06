package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record CentralInteligenciaDashboardView(
        MetaComercialDashboardView metas,
        GamificacaoDashboardView gamificacao,
        TendenciaPerformanceDashboardView tendencia,
        List<ScoreRiscoAlunoView> alunosRisco,
        List<ScoreRiscoAlunoView> alunosSemContato,
        List<CentralInteligenciaAcaoView> acoes,
        long alunosCriticos,
        long alunosAltos,
        long alunosSemContato30,
        int pontosAtuais,
        int pontosProjetados,
        String nivelProjetado,
        String prioridadeGeral,
        String resumoExecutivo
) {
    public long getAlunosEmRisco() {
        return alunosCriticos + alunosAltos;
    }

    public String getPontosProjetadosLabel() {
        return pontosProjetados + " pontos";
    }

    public String getPrioridadeCss() {
        return prioridadeGeral == null ? "media" : prioridadeGeral.toLowerCase();
    }
}
