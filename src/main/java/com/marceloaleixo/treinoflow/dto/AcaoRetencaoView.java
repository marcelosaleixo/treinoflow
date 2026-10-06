package com.marceloaleixo.treinoflow.dto;

public record AcaoRetencaoView(
        ScoreRiscoAlunoView risco,
        String mensagem,
        boolean podeEnviarWhatsApp
) {
    public String getTelefoneWhatsApp() {
        if (risco.telefone() == null) return "";
        String numero = risco.telefone().replaceAll("[^0-9]", "");
        if (numero.length() == 11) return "55" + numero;
        return numero;
    }
}
