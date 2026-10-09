package com.marceloaleixo.treinoflow.dto;

import java.math.BigDecimal;

/** Exposição financeira em aberto associada ao score operacional de retenção. */
public record ReceitaEmRiscoView(
        Long alunoId,
        String nome,
        int scoreRetencao,
        String nivelRetencao,
        BigDecimal valorPendente,
        BigDecimal valorAtrasado,
        BigDecimal exposicaoEmAberto,
        String acaoRecomendada,
        String motivoPrincipal
) {
    public String getNivelCss() {
        if (nivelRetencao == null) return "baixo";
        return nivelRetencao.toLowerCase(java.util.Locale.ROOT);
    }
}
