package com.marceloaleixo.treinoflow.dto;

/** Etapa 115: explica de forma transparente os dados usados na recomendação. */
public record RecomendacaoExplicacaoView(
        String objetivo,
        String faixaEtaria,
        String faixaRisco,
        String baseLabel,
        int diasHistorico,
        String acaoRecomendada,
        double taxaSucesso,
        long resultadosFinais,
        long amostraTotal,
        String forcaEvidencia,
        String resumo,
        String comoPodeMudar
) {
    public String getTaxaSucessoLabel() {
        return String.format(java.util.Locale.US, "%.1f%%", taxaSucesso);
    }

    public String getAmostraLabel() {
        return resultadosFinais + " resultado(s) final(is) em " + amostraTotal + " ação(ões)";
    }
}
