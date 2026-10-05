package com.marceloaleixo.treinoflow.dto;

import java.time.LocalDate;

public record CarteiraAlunoView(
        Long alunoId,
        String nome,
        String telefone,
        String segmento,
        int risco,
        String motivo,
        LocalDate ultimoTreino,
        long sessoes30Dias,
        long sessoes7Dias,
        double mediaNota,
        LocalDate ultimoContato
) {
    public String getTelefoneWhatsApp() {
        if (telefone == null) return "";
        return telefone.replaceAll("[^0-9]", "");
    }
}
