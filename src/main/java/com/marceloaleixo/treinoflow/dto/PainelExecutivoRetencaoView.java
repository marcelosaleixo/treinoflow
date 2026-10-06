package com.marceloaleixo.treinoflow.dto;

public record PainelExecutivoRetencaoView(
        long alunosAtivos,
        long riscoCritico,
        long riscoAlto,
        long riscoMedio,
        long riscoBaixo,
        long semContato30Dias,
        long followUpsPendentes,
        long recuperados,
        long cancelamentos,
        double taxaRecuperacao,
        double percentualCarteiraEmRisco
) {
    public String getTaxaRecuperacaoLabel() {
        return String.format(java.util.Locale.US, "%.1f%%", taxaRecuperacao);
    }

    public String getPercentualCarteiraEmRiscoLabel() {
        return String.format(java.util.Locale.US, "%.1f%%", percentualCarteiraEmRisco);
    }

    public long getRiscoTotal() {
        return riscoCritico + riscoAlto + riscoMedio;
    }

    public int getCriticoPercentual() { return percentual(riscoCritico); }
    public int getAltoPercentual() { return percentual(riscoAlto); }
    public int getMedioPercentual() { return percentual(riscoMedio); }
    public int getBaixoPercentual() { return percentual(riscoBaixo); }

    private int percentual(long valor) {
        if (alunosAtivos <= 0) return 0;
        return (int) Math.round(valor * 100D / alunosAtivos);
    }
}
