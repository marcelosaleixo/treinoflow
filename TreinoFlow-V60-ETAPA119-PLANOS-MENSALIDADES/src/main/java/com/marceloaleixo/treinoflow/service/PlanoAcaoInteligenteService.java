package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.PlanoAcaoInteligenteView;
import com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Etapa 106: consolida sinais operacionais em um plano de ação por aluno.
 * A inteligência apenas recomenda ações; não altera treinos, cargas ou prescrição.
 */
@Service
public class PlanoAcaoInteligenteService {
    private final AlunoRepository alunos;
    private final ScoreRiscoAlunoService riscoService;
    private final FeedbackTreinoInteligenteService feedbackService;

    public PlanoAcaoInteligenteService(AlunoRepository alunos,
                                       ScoreRiscoAlunoService riscoService,
                                       FeedbackTreinoInteligenteService feedbackService) {
        this.alunos = alunos;
        this.riscoService = riscoService;
        this.feedbackService = feedbackService;
    }

    @Transactional(readOnly = true)
    public List<PlanoAcaoInteligenteView> listar(Long personalId) {
        List<Aluno> carteira = alunos.findByPersonalIdAndStatusOrderByNomeAsc(personalId, "ATIVO");
        Map<Long, ScoreRiscoAlunoView> riscos = new HashMap<>();
        riscoService.listar(personalId).forEach(r -> riscos.put(r.alunoId(), r));

        Map<Long, FeedbackTreinoInteligenteService.FeedbackView> feedbacks = new HashMap<>();
        for (FeedbackTreinoInteligenteService.FeedbackView feedback : feedbackService.recentes(personalId)) {
            feedbacks.putIfAbsent(feedback.alunoId(), feedback);
        }

        return carteira.stream()
                .map(aluno -> montar(aluno, riscos.get(aluno.getId()), feedbacks.get(aluno.getId())))
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparingInt((PlanoAcaoInteligenteView p) -> peso(p.prioridade())).reversed()
                        .thenComparing(PlanoAcaoInteligenteView::scoreRisco, Comparator.reverseOrder())
                        .thenComparing(PlanoAcaoInteligenteView::nome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private PlanoAcaoInteligenteView montar(Aluno aluno,
                                            ScoreRiscoAlunoView risco,
                                            FeedbackTreinoInteligenteService.FeedbackView feedback) {
        int score = risco == null ? 0 : risco.score();
        String nivel = risco == null ? "baixo" : risco.nivel();
        LocalDate ultimoTreino = risco == null ? null : risco.ultimoTreino();

        boolean dorAlta = feedback != null && feedback.dor() != null && feedback.dor() >= 7;
        boolean feedbackAtencao = feedback != null && "ATENÇÃO".equals(feedback.status());
        boolean satisfacaoBaixa = feedback != null && feedback.satisfacao() != null && feedback.satisfacao() <= 2;
        boolean energiaBaixa = feedback != null && feedback.energia() != null && feedback.energia() <= 2;
        boolean sinaisFeedback = feedback != null && feedback.sinais() != null
                && !feedback.sinais().isEmpty()
                && !(feedback.sinais().size() == 1 && feedback.sinais().get(0).startsWith("Nenhum sinal"));

        String prioridade;
        String tipo;
        String titulo;
        String detalhe;
        String motivo;

        if (dorAlta) {
            prioridade = "ATENÇÃO";
            tipo = "FEEDBACK";
            titulo = "Conversar antes do próximo treino";
            motivo = feedback.sinais().get(0);
            detalhe = "Entre em contato para entender o desconforto. Se houver dor persistente ou preocupação clínica, oriente avaliação por profissional de saúde. Não altere automaticamente a prescrição.";
        } else if (feedbackAtencao || score >= 75 || (satisfacaoBaixa && energiaBaixa)) {
            prioridade = "ALTA";
            tipo = feedbackAtencao ? "FEEDBACK + RISCO" : "RISCO";
            titulo = "Priorizar contato hoje";
            motivo = motivoPrincipal(risco, feedback, "Risco elevado ou feedback com sinal importante.");
            detalhe = "Faça contato, confirme como o aluno está e registre o resultado no CRM. Antes do próximo treino, revise o contexto e a execução recente.";
        } else if (sinaisFeedback || score >= 50) {
            prioridade = "MÉDIA";
            tipo = feedback != null ? "ACOMPANHAMENTO" : "RISCO";
            titulo = "Acompanhar a próxima sessão";
            motivo = motivoPrincipal(risco, feedback, "Há sinais que merecem acompanhamento.");
            detalhe = "Confirme como o aluno chega à próxima sessão e compare o relato com frequência, execução, RPE e evolução recente.";
        } else if (score >= 25) {
            prioridade = "BAIXA";
            tipo = "PREVENÇÃO";
            titulo = "Manter acompanhamento";
            motivo = motivoPrincipal(risco, feedback, "Há um sinal preventivo de atenção.");
            detalhe = "Mantenha o acompanhamento e reavalie o contexto caso o sinal persista ou aumente.";
        } else {
            return null;
        }

        List<String> sinais = new ArrayList<>();
        if (risco != null && risco.sinais() != null) sinais.addAll(risco.sinais());
        if (feedback != null && feedback.sinais() != null) {
            for (String sinal : feedback.sinais()) {
                if (!sinais.contains(sinal) && !sinal.startsWith("Nenhum sinal")) sinais.add(sinal);
            }
        }
        if (sinais.isEmpty()) sinais.add(motivo);

        List<String> passos = passos(prioridade, feedback);
        String mensagem = mensagem(aluno.getNome(), prioridade, feedback, motivo);

        return new PlanoAcaoInteligenteView(
                aluno.getId(), aluno.getNome(), aluno.getTelefone(), prioridade, tipo,
                motivo, titulo, detalhe, score, nivel, ultimoTreino,
                feedback == null ? null : feedback.status(),
                feedback == null ? null : feedback.resumo(),
                feedback == null ? null : feedback.data(),
                sinais.stream().limit(5).toList(), passos, mensagem);
    }

    private String motivoPrincipal(ScoreRiscoAlunoView risco,
                                   FeedbackTreinoInteligenteService.FeedbackView feedback,
                                   String fallback) {
        if (feedback != null && feedback.sinais() != null) {
            for (String sinal : feedback.sinais()) {
                if (sinal != null && !sinal.startsWith("Nenhum sinal")) return sinal;
            }
        }
        if (risco != null && risco.sinais() != null && !risco.sinais().isEmpty()) {
            return risco.sinais().get(0);
        }
        return fallback;
    }

    private List<String> passos(String prioridade,
                                FeedbackTreinoInteligenteService.FeedbackView feedback) {
        List<String> passos = new ArrayList<>();
        passos.add("Abrir o contexto do aluno e entender o sinal identificado.");
        if ("ATENÇÃO".equals(prioridade) || "ALTA".equals(prioridade)) {
            passos.add("Entrar em contato pelo canal mais adequado e registrar a resposta.");
        } else {
            passos.add("Acompanhar a próxima sessão e confirmar se o sinal persiste.");
        }
        if (feedback != null && feedback.dor() != null && feedback.dor() >= 4) {
            passos.add("Reavaliar o relato de desconforto antes de considerar qualquer ajuste no treino.");
        } else {
            passos.add("Comparar o relato com execução, RPE e evolução recente.");
        }
        passos.add("Registrar no CRM o resultado e o próximo passo quando houver contato.");
        return passos;
    }

    private String mensagem(String nome,
                            String prioridade,
                            FeedbackTreinoInteligenteService.FeedbackView feedback,
                            String motivo) {
        String primeiro = nome == null || nome.isBlank() ? "tudo bem" : nome.trim().split("\\s+")[0];
        if (feedback != null && feedback.dor() != null && feedback.dor() >= 7) {
            return "Oi, " + primeiro + "! Vi seu feedback do treino e queria saber como você está. Você relatou um desconforto e quero entender melhor como se sentiu. Está tudo bem?";
        }
        if ("ALTA".equals(prioridade) || "ATENÇÃO".equals(prioridade)) {
            return "Oi, " + primeiro + "! Passando para saber como você está e como está sua rotina de treinos. Queria entender se está tudo certo e se posso te ajudar em alguma coisa.";
        }
        return "Oi, " + primeiro + "! Passando para acompanhar sua evolução. Como você está se sentindo com os treinos?";
    }

    private int peso(String prioridade) {
        return switch (prioridade) {
            case "ATENÇÃO" -> 4;
            case "ALTA" -> 3;
            case "MÉDIA" -> 2;
            default -> 1;
        };
    }
}
