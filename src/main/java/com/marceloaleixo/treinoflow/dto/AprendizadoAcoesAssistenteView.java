package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record AprendizadoAcoesAssistenteView(
        String periodoLabel,
        long totalAcoes,
        long resultadosFinais,
        String melhorAcao,
        double melhorTaxa,
        String recomendacao,
        List<DesempenhoAcaoView> desempenhos,
        List<InsightRiscoView> insightsRisco
) {
    public String getMelhorTaxaLabel() { return String.format("%.1f%%", melhorTaxa); }

    public record DesempenhoAcaoView(
            String tipo,
            long total,
            long resultadosFinais,
            long recuperados,
            long renovados,
            long cancelamentos,
            double taxaSucesso
    ) {
        public String getTaxaSucessoLabel() { return String.format("%.1f%%", taxaSucesso); }
        public String getConversaoLabel() {
            return resultadosFinais == 0 ? "Sem resultado final" : String.format("%.1f%%", (recuperados + renovados) * 100.0 / resultadosFinais);
        }
    }

    public record InsightRiscoView(
            String faixa,
            long total,
            String melhorAcao,
            double taxaMelhorAcao,
            String leitura
    ) {
        public String getTaxaMelhorAcaoLabel() { return String.format("%.1f%%", taxaMelhorAcao); }
    }
}
