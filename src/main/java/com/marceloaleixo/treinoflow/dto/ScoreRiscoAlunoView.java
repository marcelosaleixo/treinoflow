package com.marceloaleixo.treinoflow.dto;

import java.time.LocalDate;
import java.util.List;

public record ScoreRiscoAlunoView(
        Long alunoId,
        String nome,
        String telefone,
        int score,
        String nivel,
        long faltas30Dias,
        long realizados30Dias,
        double taxaComparecimento,
        long treinos30Dias,
        Double mediaNota,
        LocalDate ultimoTreino,
        long diasSemTreinar,
        long diasSemContato,
        boolean quedaCarga,
        double quedaCargaPercentual,
        double quedaFrequenciaPercentual,
        List<String> sinais,
        String acaoRecomendada
) {
    public String getNivelCss() {
        return nivel == null ? "baixo" : nivel.toLowerCase();
    }

    public String getTelefoneWhatsApp() {
        if (telefone == null) return "";
        String numero = telefone.replaceAll("[^0-9]", "");
        if (numero.length() == 11) return "55" + numero;
        return numero;
    }

    public String getScoreLabel() {
        return score + "/100";
    }

    public String getMediaNotaLabel() {
        return mediaNota == null ? "Sem avaliação" : String.format(java.util.Locale.US, "%.1f/5", mediaNota);
    }

    public String getTaxaLabel() {
        return String.format(java.util.Locale.US, "%.1f%%", taxaComparecimento);
    }

    public String getQuedaCargaLabel() {
        return quedaCarga ? String.format(java.util.Locale.US, "%.1f%%", quedaCargaPercentual) : "Sem queda relevante";
    }

    public String getQuedaFrequenciaLabel() {
        if (quedaFrequenciaPercentual <= 0D) return "Sem queda relevante";
        return String.format(java.util.Locale.US, "%.1f%%", quedaFrequenciaPercentual);
    }
}
