package com.marceloaleixo.treinoflow.dto;

import com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente;

/** Etapa 116: desempenho comparativo das estratégias de retenção. */
public record ComparadorEstrategiaView(
        TipoAcaoAssistente tipo,
        long totalAcoes,
        long resultadosFinais,
        long positivos,
        double taxaSucesso,
        String forcaEvidencia,
        boolean melhor,
        String leitura
) {
    public String getTipoLabel() {
        return tipo == null ? "Estratégia" : tipo.getDescricao();
    }

    public String getTaxaSucessoLabel() {
        return String.format(java.util.Locale.US, "%.1f%%", taxaSucesso);
    }

    public String getAmostraLabel() {
        return resultadosFinais + " resultado(s) final(is) / " + totalAcoes + " ação(ões)";
    }
}
