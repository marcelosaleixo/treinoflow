package com.marceloaleixo.treinoflow.dto;

/** Etapa 117: recomendação com nível de confiança explicável. */
public record RecomendacaoAcaoPerfilView(
        String acao,
        String titulo,
        String motivo,
        double taxaSucesso,
        long resultadosFinais,
        long amostraTotal,
        String base,
        boolean suficiente,
        String aviso,
        int confianca,
        String nivelConfianca,
        String resumoConfianca,
        String motivosConfianca
) {
    public String getTaxaSucessoLabel() {
        return String.format(java.util.Locale.US, "%.1f%%", taxaSucesso);
    }

    public String getAmostraLabel() {
        return resultadosFinais + " resultado(s) final(is) em " + amostraTotal + " ação(ões)";
    }

    public String getBaseLabel() {
        return switch (base == null ? "GERAL" : base) {
            case "PERFIL_EXATO" -> "mesmo perfil + faixa de risco";
            case "OBJETIVO_RISCO" -> "mesmo objetivo + faixa de risco";
            case "RISCO" -> "mesma faixa de risco";
            default -> "histórico geral do Personal";
        };
    }

    public String getConfiancaLabel() {
        return confianca + "%";
    }
}
