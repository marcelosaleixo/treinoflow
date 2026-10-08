package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.RegistroTreinoAluno;
import com.marceloaleixo.treinoflow.repository.RegistroTreinoAlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class FeedbackTreinoInteligenteService {
    private final RegistroTreinoAlunoRepository registros;

    public FeedbackTreinoInteligenteService(RegistroTreinoAlunoRepository registros) {
        this.registros = registros;
    }

    @Transactional(readOnly = true)
    public List<FeedbackView> recentes(Long personalId) {
        return registros.findTop30ByAlunoPersonalIdOrderByDataRegistroDesc(personalId).stream()
                .filter(RegistroTreinoAluno::isConcluido)
                .filter(r -> r.getNota() != null || r.getFeedback() != null || r.getDor() != null || r.getEsforco() != null || r.getEnergia() != null)
                .limit(10)
                .map(this::mapear)
                .toList();
    }

    private FeedbackView mapear(RegistroTreinoAluno r) {
        List<String> sinais = new ArrayList<>();
        List<String> acoes = new ArrayList<>();

        Integer dor = r.getDor();
        Integer esforco = r.getEsforco();
        Integer energia = r.getEnergia();
        Integer satisfacao = r.getNota();

        if (dor != null && dor >= 7) {
            sinais.add("Dor/desconforto elevado (" + dor + "/10)");
            acoes.add("Entrar em contato para entender o desconforto e revisar o treino se necessário.");
        } else if (dor != null && dor >= 4) {
            sinais.add("Desconforto moderado (" + dor + "/10)");
            acoes.add("Acompanhar o próximo treino e confirmar se o desconforto persistiu.");
        }
        if (esforco != null && esforco >= 5) {
            sinais.add("Esforço percebido muito alto (" + esforco + "/5)");
            acoes.add("Observar recuperação e desempenho nas próximas sessões antes de aumentar a carga.");
        }
        if (energia != null && energia <= 2) {
            sinais.add("Energia baixa (" + energia + "/5)");
            acoes.add("Perguntar como está a recuperação e acompanhar a próxima sessão.");
        }
        if (satisfacao != null && satisfacao <= 2) {
            sinais.add("Satisfação baixa (" + satisfacao + "/5)");
            acoes.add("Fazer contato para entender a experiência do aluno.");
        }

        String status;
        if (dor != null && dor >= 7) status = "ATENÇÃO";
        else if (!sinais.isEmpty()) status = "ACOMPANHAR";
        else if (satisfacao != null && satisfacao >= 4) status = "POSITIVO";
        else status = "ESTÁVEL";

        if (sinais.isEmpty()) {
            sinais.add("Nenhum sinal de atenção identificado no feedback.");
        }
        if (acoes.isEmpty()) {
            acoes.add("Manter o acompanhamento normal e observar a evolução do próximo treino.");
        }

        String resumo = switch (status) {
            case "ATENÇÃO" -> "Feedback indica um sinal que merece contato do Personal antes de qualquer ajuste.";
            case "ACOMPANHAR" -> "Há sinais para acompanhar na próxima sessão, sem conclusão automática sobre a causa.";
            case "POSITIVO" -> "Aluno reportou uma experiência positiva no treino.";
            default -> "Feedback sem sinal relevante para ação imediata.";
        };

        return new FeedbackView(
                r.getAluno().getId(),
                r.getAluno().getNome(),
                r.getTreino().getNome(),
                r.getDataExecucao(),
                satisfacao,
                esforco,
                dor,
                energia,
                status,
                resumo,
                sinais,
                acoes,
                r.getFeedback()
        );
    }

    public record FeedbackView(
            Long alunoId,
            String alunoNome,
            String treinoNome,
            java.time.LocalDate data,
            Integer satisfacao,
            Integer esforco,
            Integer dor,
            Integer energia,
            String status,
            String resumo,
            List<String> sinais,
            List<String> acoes,
            String observacao
    ) {}
}
