package com.marceloaleixo.treinoflow.dto;

/** Etapa 111: comparação do risco antes e depois da ação do Radar Diário. */
public record RadarReavaliacaoView(
        Long alunoId,
        String alunoNome,
        int scoreAntes,
        String nivelAntes,
        String prioridadeAntes,
        int scoreDepois,
        String nivelDepois,
        String prioridadeDepois,
        int variacaoScore,
        String tendencia,
        String resultado,
        String proximaAcao,
        String observacao,
        String novaRecomendacao
) {
    public String tendenciaCss() {
        if (variacaoScore < 0) return "positive";
        if (variacaoScore > 0) return "negative";
        return "neutral";
    }

    public String prioridadeDepoisCss() {
        return switch (prioridadeDepois) {
            case "ATENÇÃO" -> "danger";
            case "ALTA" -> "high";
            case "MÉDIA" -> "warn";
            default -> "good";
        };
    }
}
