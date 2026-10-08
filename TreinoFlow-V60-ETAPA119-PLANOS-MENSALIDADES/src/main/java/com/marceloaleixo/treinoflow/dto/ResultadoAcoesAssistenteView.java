package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record ResultadoAcoesAssistenteView(
        String periodoLabel,
        long total,
        long recuperados,
        long renovados,
        long emAcompanhamento,
        long semResposta,
        long cancelamentos,
        double taxaSucesso,
        List<AcaoResultadoItemView> acoes
) {
    public record AcaoResultadoItemView(
            Long id,
            String alunoNome,
            String tipo,
            String resultado,
            int scoreRisco,
            String executadaEm,
            String descricao
    ) {}

    public String getTaxaSucessoLabel() { return String.format("%.1f%%", taxaSucesso); }
}
