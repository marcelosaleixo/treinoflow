package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Etapa 105: transforma o feedback pós-treino em uma fila de ações para o Personal.
 * Não altera prescrição nem executa contato automaticamente.
 */
@Service
public class PosTreinoAcaoService {
    private final FeedbackTreinoInteligenteService feedbackService;
    private final ScoreRiscoAlunoService riscoService;

    public PosTreinoAcaoService(FeedbackTreinoInteligenteService feedbackService,
                                 ScoreRiscoAlunoService riscoService) {
        this.feedbackService = feedbackService;
        this.riscoService = riscoService;
    }

    @Transactional(readOnly = true)
    public CentralPosTreino montar(Long personalId) {
        List<FeedbackTreinoInteligenteService.FeedbackView> feedbacks = feedbackService.recentes(personalId);
        Map<Long, ScoreRiscoAlunoView> riscos = new HashMap<>();
        riscoService.listar(personalId).forEach(r -> riscos.put(r.alunoId(), r));

        List<AcaoPosTreino> acoes = feedbacks.stream()
                .map(f -> montarAcao(f, riscos.get(f.alunoId())))
                .filter(a -> !"BAIXA".equals(a.prioridade()))
                .sorted(Comparator.comparingInt((AcaoPosTreino a) -> peso(a.prioridade())).reversed()
                        .thenComparing(AcaoPosTreino::data, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .toList();

        long atencao = acoes.stream().filter(a -> "ATENÇÃO".equals(a.prioridade())).count();
        long alta = acoes.stream().filter(a -> "ALTA".equals(a.prioridade())).count();
        long media = acoes.stream().filter(a -> "MÉDIA".equals(a.prioridade())).count();
        return new CentralPosTreino(acoes, atencao, alta, media);
    }

    private AcaoPosTreino montarAcao(FeedbackTreinoInteligenteService.FeedbackView f,
                                     ScoreRiscoAlunoView risco) {
        int score = risco == null ? 0 : risco.score();
        String prioridade;
        String titulo;
        String acao;

        if (f.dor() != null && f.dor() >= 7) {
            prioridade = "ATENÇÃO";
            titulo = "Conversar sobre o desconforto";
            acao = "Entrar em contato antes do próximo treino para entender o relato. Se houver dor persistente ou preocupação clínica, orientar avaliação por profissional de saúde. Não alterar automaticamente a prescrição.";
        } else if (score >= 75 || (f.satisfacao() != null && f.satisfacao() <= 2 && f.energia() != null && f.energia() <= 2)) {
            prioridade = "ALTA";
            titulo = "Priorizar acompanhamento do aluno";
            acao = "Abrir o histórico do aluno, verificar contexto e registrar a ação no CRM. Avaliar o próximo treino antes de qualquer ajuste.";
        } else if ((f.dor() != null && f.dor() >= 4) || (f.esforco() != null && f.esforco() >= 5) || (f.energia() != null && f.energia() <= 2) || score >= 50) {
            prioridade = "MÉDIA";
            titulo = "Acompanhar a próxima sessão";
            acao = "Confirmar como o aluno chega ao próximo treino e comparar com execução, RPE e evolução recente.";
        } else {
            prioridade = "BAIXA";
            titulo = "Manter acompanhamento";
            acao = "Sem ação imediata. Continue acompanhando a evolução normalmente.";
        }

        return new AcaoPosTreino(f.alunoId(), f.alunoNome(), f.treinoNome(), f.data(), prioridade,
                titulo, acao, f.resumo(), f.sinais(), score);
    }

    private int peso(String prioridade) {
        return switch (prioridade) {
            case "ATENÇÃO" -> 4;
            case "ALTA" -> 3;
            case "MÉDIA" -> 2;
            default -> 1;
        };
    }

    public record CentralPosTreino(List<AcaoPosTreino> acoes, long atencao, long alta, long media) {}

    public record AcaoPosTreino(Long alunoId, String alunoNome, String treinoNome,
                                java.time.LocalDate data, String prioridade, String titulo,
                                String acao, String resumo, List<String> sinais, int scoreRisco) {
        public String getPrioridadeCss() {
            return switch (prioridade) {
                case "ATENÇÃO" -> "danger";
                case "ALTA" -> "high";
                case "MÉDIA" -> "warn";
                default -> "good";
            };
        }
    }
}
