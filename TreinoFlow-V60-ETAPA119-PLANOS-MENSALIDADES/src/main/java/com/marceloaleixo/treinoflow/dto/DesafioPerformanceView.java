package com.marceloaleixo.treinoflow.dto;

public record DesafioPerformanceView(
        Long id,
        String titulo,
        String descricao,
        int meta,
        long atual,
        int progresso,
        int bonusPontos,
        boolean concluido,
        String status,
        String atualLabel,
        String metaLabel
) {
    public String getProgressoLabel() { return progresso + "%"; }
}
