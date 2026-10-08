package com.marceloaleixo.treinoflow.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Etapa 106: plano operacional consolidado por aluno.
 * Reúne risco, feedback pós-treino e próxima ação sem alterar a prescrição automaticamente.
 */
public record PlanoAcaoInteligenteView(
        Long alunoId,
        String nome,
        String telefone,
        String prioridade,
        String tipo,
        String motivoPrincipal,
        String acaoTitulo,
        String acaoDetalhada,
        int scoreRisco,
        String nivelRisco,
        LocalDate ultimoTreino,
        String feedbackStatus,
        String feedbackResumo,
        LocalDate feedbackData,
        List<String> sinais,
        List<String> passos,
        String mensagemWhatsApp
) {
    public String getPrioridadeCss() {
        return switch (prioridade) {
            case "ATENÇÃO" -> "danger";
            case "ALTA" -> "high";
            case "MÉDIA" -> "warn";
            default -> "good";
        };
    }

    public String getTelefoneWhatsApp() {
        if (telefone == null) return "";
        String numero = telefone.replaceAll("[^0-9]", "");
        if (numero.length() == 11) return "55" + numero;
        return numero;
    }

    public String getWhatsAppUrl() {
        if (getTelefoneWhatsApp().isBlank()) return "";
        try {
            return "https://wa.me/" + getTelefoneWhatsApp() + "?text="
                    + java.net.URLEncoder.encode(mensagemWhatsApp, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return "https://wa.me/" + getTelefoneWhatsApp();
        }
    }

    public String getMotivoCrm() {
        return motivoPrincipal == null ? "Sinal identificado pelo plano de ação inteligente." : motivoPrincipal;
    }

    public boolean hasFeedback() {
        return feedbackStatus != null && !feedbackStatus.isBlank();
    }
}
