package com.marceloaleixo.treinoflow.dto;

public record MetaComercialDashboardView(
        String mesLabel,
        int metaAlunosAtivos, long alunosAtivos,
        int metaTreinos, long treinosRealizados,
        int metaRecuperacoes, long recuperacoes,
        int percentualAlunos, int percentualTreinos, int percentualRecuperacoes,
        int progressoGeral, String nivel, String mensagem
) {
    public String getProgressoAlunosLabel() { return percentualAlunos + "%"; }
    public String getProgressoTreinosLabel() { return percentualTreinos + "%"; }
    public String getProgressoRecuperacoesLabel() { return percentualRecuperacoes + "%"; }
    public String getProgressoGeralLabel() { return progressoGeral + "%"; }
}
