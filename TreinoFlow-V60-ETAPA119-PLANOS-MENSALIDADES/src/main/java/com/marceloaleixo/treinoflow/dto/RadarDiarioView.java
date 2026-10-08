package com.marceloaleixo.treinoflow.dto;

import java.time.LocalDate;
import java.util.List;

/** Etapa 109: visão consolidada do que merece atenção no dia. */
public record RadarDiarioView(
        String prioridade,
        String tipo,
        Long alunoId,
        String alunoNome,
        String titulo,
        String motivo,
        String acao,
        String status,
        LocalDate dataReferencia,
        int scoreRisco,
        String whatsappUrl
) {
    public String prioridadeCss() {
        return switch (prioridade) {
            case "ATENÇÃO" -> "danger";
            case "ALTA" -> "high";
            case "MÉDIA" -> "warn";
            default -> "good";
        };
    }
}
