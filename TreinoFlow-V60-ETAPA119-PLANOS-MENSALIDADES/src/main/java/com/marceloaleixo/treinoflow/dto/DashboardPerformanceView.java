package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record DashboardPerformanceView(
        long sessoes30Dias,
        long sessoes7Dias,
        double mediaNotas,
        long alunosComTreino30Dias,
        long alunosSemTreino30Dias,
        List<SemanaPerformanceView> semanas,
        List<AlunoPerformanceView> alunosEmRisco,
        List<AlunoPerformanceView> alunosMaisAtivos,
        List<RecomendacaoPerformanceView> recomendacoes
) {
    public double percentualAdesao() {
        long total = alunosComTreino30Dias + alunosSemTreino30Dias;
        return total == 0 ? 0D : (alunosComTreino30Dias * 100.0) / total;
    }

    public record SemanaPerformanceView(String periodo, long sessoes) {
        public int alturaBarra() {
            if (sessoes <= 0) {
                return 4;
            }
            return (int) Math.min(100, sessoes * 12);
        }
    }
}
