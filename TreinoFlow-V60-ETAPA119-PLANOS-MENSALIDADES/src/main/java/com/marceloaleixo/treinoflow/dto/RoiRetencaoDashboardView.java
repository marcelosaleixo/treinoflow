package com.marceloaleixo.treinoflow.dto;

import java.math.BigDecimal;
import java.util.List;

public record RoiRetencaoDashboardView(
        String periodoLabel, long acoesAutomaticas, long acoesManuais, long contatos,
        long respostas, long recuperados, long renovados, long cancelamentos,
        double taxaRecuperacao, BigDecimal valorRecuperadoEstimado, BigDecimal custoEstimado,
        BigDecimal resultadoEstimado, Double roiPercentual, String melhorCanal,
        List<FaixaRiscoView> faixas) {
    public record FaixaRiscoView(String faixa, long acoes, long recuperacoes, double taxa) {}
    public String getValorRecuperadoLabel(){ return moeda(valorRecuperadoEstimado); }
    public String getCustoLabel(){ return moeda(custoEstimado); }
    public String getResultadoLabel(){ return moeda(resultadoEstimado); }
    public String getRoiLabel(){ return roiPercentual == null ? "Não calculado" : String.format("%.1f%%", roiPercentual); }
    private String moeda(BigDecimal v){ return "R$ " + v.setScale(2, java.math.RoundingMode.HALF_UP).toString().replace('.', ','); }
}
