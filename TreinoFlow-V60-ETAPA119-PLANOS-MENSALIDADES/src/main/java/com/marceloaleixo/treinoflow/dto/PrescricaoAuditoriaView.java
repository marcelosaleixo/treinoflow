package com.marceloaleixo.treinoflow.dto;

import com.marceloaleixo.treinoflow.entity.PrescricaoAuditoria;
import java.time.format.DateTimeFormatter;

public record PrescricaoAuditoriaView(
        Long id, String exercicioNome, String nivelIa, String confiancaIa, String justificativa,
        String seriesAntes, String repeticoesAntes, String cargaAntes,
        String seriesDepois, String repeticoesDepois, String cargaDepois,
        String decisao, String dataDecisao, String feedbackDecisao, String feedbackMotivo, String feedbackData) {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static PrescricaoAuditoriaView from(PrescricaoAuditoria a) {
        return new PrescricaoAuditoriaView(a.getId(), a.getExercicioNome(), a.getNivelIa(), a.getConfiancaIa(), a.getJustificativa(),
                a.getSeriesAntes(), a.getRepeticoesAntes(), a.getCargaAntes(),
                a.getSeriesDepois(), a.getRepeticoesDepois(), a.getCargaDepois(),
                a.getDecisao(), a.getDataDecisao() == null ? "—" : a.getDataDecisao().format(FMT),
                a.getFeedbackDecisao(), a.getFeedbackMotivo(),
                a.getFeedbackData() == null ? null : a.getFeedbackData().format(FMT));
    }
}
