package com.marceloaleixo.treinoflow.dto;

public record AcaoInteligenteAlunoView(
        Long alunoId,
        String nome,
        String telefone,
        String segmento,
        int risco,
        String motivo,
        String acao,
        String prioridade,
        String mensagemWhatsApp
) {
    public String getTelefoneWhatsApp() {
        if (telefone == null) return "";
        return telefone.replaceAll("[^0-9]", "");
    }
}
