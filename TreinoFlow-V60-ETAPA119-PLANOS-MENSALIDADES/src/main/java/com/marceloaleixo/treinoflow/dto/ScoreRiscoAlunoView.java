package com.marceloaleixo.treinoflow.dto;

import java.time.LocalDate;
import java.util.List;

public record ScoreRiscoAlunoView(
        Long alunoId,
        String nome,
        String telefone,
        int score,
        String nivel,
        long faltas30Dias,
        long realizados30Dias,
        double taxaComparecimento,
        long treinos30Dias,
        Double mediaNota,
        LocalDate ultimoTreino,
        long diasSemTreinar,
        long diasSemContato,
        boolean quedaCarga,
        double quedaCargaPercentual,
        double quedaFrequenciaPercentual,
        List<String> sinais,
        String acaoRecomendada
) {
    public String getNivelCss() {
        return nivel == null ? "baixo" : nivel.toLowerCase();
    }

    public String getTelefoneWhatsApp() {
        if (telefone == null) return "";
        String numero = telefone.replaceAll("[^0-9]", "");
        if (numero.length() == 11) return "55" + numero;
        return numero;
    }

    public String getScoreLabel() {
        return score + "/100";
    }

    public String getMediaNotaLabel() {
        return mediaNota == null ? "Sem avaliação" : String.format(java.util.Locale.US, "%.1f/5", mediaNota);
    }

    public String getTaxaLabel() {
        return String.format(java.util.Locale.US, "%.1f%%", taxaComparecimento);
    }

    public String getQuedaCargaLabel() {
        return quedaCarga ? String.format(java.util.Locale.US, "%.1f%%", quedaCargaPercentual) : "Sem queda relevante";
    }


    public String getAcaoTitulo() {
        if (score >= 75) return "Priorizar contato hoje";
        if (score >= 50) return "Fazer contato preventivo";
        if (score >= 25) return "Acompanhar de perto";
        return "Manter acompanhamento";
    }

    public String getMotivoPrincipal() {
        if (sinais == null || sinais.isEmpty()) return "Nenhum sinal relevante identificado.";
        return sinais.get(0);
    }

    public String getMensagemWhatsApp() {
        String primeiroNome = nome == null || nome.isBlank() ? "tudo bem" : nome.trim().split("\\s+")[0];
        if (score >= 75) return "Oi, " + primeiroNome + "! Percebi que faz um tempo desde seu último treino. Está tudo bem? Queria saber como você está e se posso te ajudar a retomar sua rotina.";
        if (score >= 50) return "Oi, " + primeiroNome + "! Como você está? Notei uma mudança recente na sua rotina de treinos e queria entender se está tudo bem ou se precisamos ajustar alguma coisa.";
        return "Oi, " + primeiroNome + "! Passando para saber como está sua evolução e se está tudo certo com seus treinos.";
    }

    public String getWhatsAppUrl() {
        if (getTelefoneWhatsApp().isBlank()) return "";
        try {
            return "https://wa.me/" + getTelefoneWhatsApp() + "?text=" + java.net.URLEncoder.encode(getMensagemWhatsApp(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return "https://wa.me/" + getTelefoneWhatsApp();
        }
    }

    public String getQuedaFrequenciaLabel() {
        if (quedaFrequenciaPercentual <= 0D) return "Sem queda relevante";
        return String.format(java.util.Locale.US, "%.1f%%", quedaFrequenciaPercentual);
    }
}
