package com.marceloaleixo.treinoflow.dto;

public record CentralInteligenciaAcaoView(
        String prioridade,
        String titulo,
        String descricao,
        String link,
        String linkLabel
) {
    public String getPrioridadeCss() {
        return prioridade == null ? "media" : prioridade.toLowerCase();
    }
}
