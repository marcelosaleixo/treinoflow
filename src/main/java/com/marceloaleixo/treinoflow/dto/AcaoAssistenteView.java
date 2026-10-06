package com.marceloaleixo.treinoflow.dto;

public record AcaoAssistenteView(
        ScoreRiscoAlunoView risco,
        String prioridade,
        String titulo,
        String motivo,
        String mensagem,
        boolean podeEnviarWhatsApp,
        String acaoAdaptativa,
        String justificativaAdaptativa,
        String confiancaAdaptativa
) {
    public String getPrioridadeCss() {
        return prioridade == null ? "media" : prioridade.toLowerCase();
    }
}
