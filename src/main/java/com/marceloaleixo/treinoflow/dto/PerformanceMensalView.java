package com.marceloaleixo.treinoflow.dto;

public record PerformanceMensalView(
        String mesLabel,
        int pontos,
        String nivel,
        int progressoGeral,
        int pontosMetas,
        int pontosTreinos,
        int pontosRecuperacoes,
        int pontosAcompanhamento,
        long alunosAcompanhados,
        long treinosRealizados,
        long recuperacoes,
        int bonusDesafios,
        int desafiosConcluidos
) {
    public String getPontosLabel() { return pontos + " pontos"; }
    public String getProgressoLabel() { return progressoGeral + "%"; }
}
