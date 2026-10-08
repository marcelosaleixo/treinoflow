package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record GamificacaoDashboardView(
        String mesLabel,
        int pontos,
        String nivel,
        String proximoNivel,
        int pontosProximoNivel,
        int progressoNivel,
        int pontosMetas,
        int pontosTreinos,
        int pontosRecuperacoes,
        int pontosCarteira,
        List<String> conquistas,
        String mensagem
) {
    public String getPontosLabel() { return pontos + " pontos"; }
    public String getProgressoNivelLabel() { return progressoNivel + "%"; }
}
