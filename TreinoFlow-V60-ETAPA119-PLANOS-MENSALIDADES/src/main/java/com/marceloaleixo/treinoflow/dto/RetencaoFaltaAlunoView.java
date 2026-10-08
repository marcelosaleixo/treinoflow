package com.marceloaleixo.treinoflow.dto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public record RetencaoFaltaAlunoView(
        Long alunoId,
        String nome,
        String telefone,
        long faltas30Dias,
        long realizados30Dias,
        double taxaComparecimento,
        LocalDateTime ultimoAgendamento,
        String prioridade,
        String motivo,
        String mensagemWhatsApp
) {
    public String getLinkWhatsApp() {
        String numero = getTelefoneWhatsApp();
        if (numero.isBlank()) return "";
        return "https://wa.me/" + numero + "?text=" + URLEncoder.encode(mensagemWhatsApp == null ? "" : mensagemWhatsApp, StandardCharsets.UTF_8);
    }

    public String getTelefoneWhatsApp() {
        if (telefone == null) return "";
        String numero = telefone.replaceAll("[^0-9]", "");
        if (numero.length() == 11) return "55" + numero;
        return numero;
    }
}
