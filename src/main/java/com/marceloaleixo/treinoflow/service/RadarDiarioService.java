package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.FollowUpInteligenteView;
import com.marceloaleixo.treinoflow.dto.RadarDiarioView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Etapa 109: consolida sinais operacionais em um radar diário.
 * Apenas prioriza e navega; não executa contatos nem altera prescrição.
 */
@Service
public class RadarDiarioService {
    private final ScoreRiscoAlunoService riscoService;
    private final FollowUpInteligenteService followUpService;
    private final FeedbackTreinoInteligenteService feedbackService;

    public RadarDiarioService(ScoreRiscoAlunoService riscoService,
                              FollowUpInteligenteService followUpService,
                              FeedbackTreinoInteligenteService feedbackService) {
        this.riscoService = riscoService;
        this.followUpService = followUpService;
        this.feedbackService = feedbackService;
    }

    @Transactional(readOnly = true)
    public Radar montar(Long personalId) {
        LocalDate hoje = LocalDate.now();
        var riscos = riscoService.listar(personalId);
        Map<Long, com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView> riscoPorAluno = new HashMap<>();
        riscos.forEach(r -> riscoPorAluno.put(r.alunoId(), r));

        List<RadarDiarioView> itens = new ArrayList<>();
        List<FollowUpInteligenteView> followUps = followUpService.listar(personalId);

        for (FollowUpInteligenteView f : followUps) {
            if (!"PRÓXIMO".equals(f.statusPrazo())) {
                var risco = riscoPorAluno.get(f.alunoId());
                itens.add(new RadarDiarioView(
                        "VENCIDO".equals(f.statusPrazo()) && f.diasAtraso() >= 3 ? "ATENÇÃO" : "ALTA",
                        "FOLLOW-UP",
                        f.alunoId(), f.alunoNome(),
                        "Executar follow-up",
                        f.statusPrazo() + (f.diasAtraso() > 0 ? " há " + f.diasAtraso() + " dia(s)" : ""),
                        f.acaoSugerida(), f.statusPrazo(), f.dataProximaAcao(),
                        risco == null ? 0 : risco.score(), f.whatsappUrl()));
            }
        }

        var feedbacks = feedbackService.recentes(personalId);
        for (var f : feedbacks) {
            if (!hoje.minusDays(7).isBefore(f.data())) continue;
            boolean atencao = f.dor() != null && f.dor() >= 7;
            boolean alta = "ATENÇÃO".equals(f.status()) || (f.satisfacao() != null && f.satisfacao() <= 2 && f.energia() != null && f.energia() <= 2);
            if (!atencao && !alta) continue;
            var risco = riscoPorAluno.get(f.alunoId());
            String primeiro = primeiroNome(f.alunoNome());
            String motivo = f.sinais() == null || f.sinais().isEmpty() ? f.resumo() : f.sinais().get(0);
            itens.add(new RadarDiarioView(
                    atencao ? "ATENÇÃO" : "ALTA",
                    "FEEDBACK",
                    f.alunoId(), f.alunoNome(),
                    atencao ? "Revisar feedback de dor" : "Acompanhar feedback",
                    motivo,
                    atencao ? "Conversar com " + primeiro + " antes do próximo treino e entender o relato."
                            : "Entrar em contato e confirmar como o aluno está antes da próxima sessão.",
                    f.status(), f.data(), risco == null ? 0 : risco.score(), whatsappUrl(f.alunoNome(), risco == null ? null : risco.telefone())));
        }

        for (var risco : riscos) {
            if (risco.score() < 50) continue;
            String prioridade = risco.score() >= 75 ? "ATENÇÃO" : "ALTA";
            itens.add(new RadarDiarioView(
                    prioridade,
                    "RISCO",
                    risco.alunoId(), risco.nome(),
                    risco.score() >= 75 ? "Priorizar contato hoje" : "Fazer contato preventivo",
                    risco.getMotivoPrincipal(),
                    risco.getAcaoTitulo(),
                    risco.nivel(), risco.ultimoTreino(), risco.score(), risco.getWhatsAppUrl()));
        }

        itens = itens.stream()
                .filter(i -> i.alunoId() != null)
                .sorted(Comparator.comparingInt((RadarDiarioView i) -> peso(i.prioridade())).reversed()
                        .thenComparing(Comparator.comparingInt(RadarDiarioView::scoreRisco).reversed())
                        .thenComparing(RadarDiarioView::alunoNome, String.CASE_INSENSITIVE_ORDER))
                .distinct()
                .limit(12)
                .toList();

        long atencao = itens.stream().filter(i -> "ATENÇÃO".equals(i.prioridade())).count();
        long alta = itens.stream().filter(i -> "ALTA".equals(i.prioridade())).count();
        long followUpsHoje = followUps.stream().filter(f -> "HOJE".equals(f.statusPrazo())).count();
        long vencidos = followUps.stream().filter(f -> "VENCIDO".equals(f.statusPrazo())).count();
        return new Radar(itens, atencao, alta, followUpsHoje, vencidos, itens.stream().findFirst().orElse(null));
    }

    private int peso(String prioridade) {
        return switch (prioridade) {
            case "ATENÇÃO" -> 4;
            case "ALTA" -> 3;
            case "MÉDIA" -> 2;
            default -> 1;
        };
    }

    private String primeiroNome(String nome) {
        return nome == null || nome.isBlank() ? "você" : nome.trim().split("\\s+")[0];
    }

    private String whatsappUrl(String nome, String telefone) {
        if (telefone == null || telefone.isBlank()) return "";
        String numero = telefone.replaceAll("\\D", "");
        if (numero.isBlank()) return "";
        String mensagem = "Oi, " + primeiroNome(nome) + "! Passando para saber como você está e acompanhar seus treinos. Está tudo bem?";
        return "https://wa.me/" + numero + "?text=" + URLEncoder.encode(mensagem, StandardCharsets.UTF_8);
    }

    public record Radar(List<RadarDiarioView> itens,
                        long atencao,
                        long alta,
                        long followUpsHoje,
                        long followUpsVencidos,
                        RadarDiarioView prioridadeUm) {}
}
