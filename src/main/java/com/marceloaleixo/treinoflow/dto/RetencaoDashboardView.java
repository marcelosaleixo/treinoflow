package com.marceloaleixo.treinoflow.dto;

import com.marceloaleixo.treinoflow.entity.InteracaoCrm;

import java.util.List;

public record RetencaoDashboardView(
        long acoesVencidas,
        long acoesHoje,
        long acoesProximos7Dias,
        long recuperados30Dias,
        long renovados30Dias,
        long cancelamentos30Dias,
        List<InteracaoCrm> vencidas,
        List<InteracaoCrm> hoje,
        List<InteracaoCrm> proximos7Dias,
        List<InteracaoCrm> recuperados,
        List<InteracaoCrm> renovados,
        List<InteracaoCrm> cancelamentos
) {
    public long totalFila() {
        return acoesVencidas + acoesHoje + acoesProximos7Dias;
    }

    public long totalResultados30Dias() {
        return recuperados30Dias + renovados30Dias + cancelamentos30Dias;
    }

    public double taxaSucesso30Dias() {
        long sucesso = recuperados30Dias + renovados30Dias;
        long total = sucesso + cancelamentos30Dias;
        return total == 0 ? 0.0 : sucesso * 100.0 / total;
    }
}
