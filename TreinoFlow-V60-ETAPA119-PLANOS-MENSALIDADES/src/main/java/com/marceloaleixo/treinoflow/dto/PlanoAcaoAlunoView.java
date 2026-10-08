package com.marceloaleixo.treinoflow.dto;

import java.time.LocalDate;

public record PlanoAcaoAlunoView(
        Long alunoId,
        String nome,
        String telefone,
        String prioridade,
        String tipo,
        String motivo,
        String acao,
        LocalDate ultimoTreino,
        long sessoes30Dias,
        long sessoes7Dias,
        double mediaNota
) {
    public String getTelefoneWhatsApp() {
        if (telefone == null) return "";
        return telefone.replaceAll("[^0-9]", "");
    }
}
