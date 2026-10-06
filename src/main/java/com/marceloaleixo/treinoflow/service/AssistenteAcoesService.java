package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.AcaoAssistenteView;
import com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class AssistenteAcoesService {
    private final ScoreRiscoAlunoService scoreRisco;
    private final RetencaoInteligenteService retencao;
    private final RecomendacaoAdaptativaService recomendacaoAdaptativa;

    public AssistenteAcoesService(ScoreRiscoAlunoService scoreRisco,
                                  RetencaoInteligenteService retencao,
                                  RecomendacaoAdaptativaService recomendacaoAdaptativa) {
        this.scoreRisco = scoreRisco;
        this.retencao = retencao;
        this.recomendacaoAdaptativa = recomendacaoAdaptativa;
    }

    @Transactional(readOnly = true)
    public List<AcaoAssistenteView> listar(Long personalId) {
        return scoreRisco.listar(personalId).stream()
                .filter(r -> r.score() >= 25)
                .sorted(Comparator.comparingInt(ScoreRiscoAlunoView::score).reversed()
                        .thenComparing(ScoreRiscoAlunoView::diasSemContato, Comparator.reverseOrder()))
                .map(r -> criarAcao(personalId, r))
                .toList();
    }

    private AcaoAssistenteView criarAcao(Long personalId, ScoreRiscoAlunoView risco) {
        String prioridade;
        String titulo;
        String motivo;

        if (risco.score() >= 75) {
            prioridade = "CRÍTICA";
            titulo = "Recuperar este aluno hoje";
            motivo = "Risco crítico: " + resumoSinais(risco);
        } else if (risco.score() >= 50) {
            prioridade = "ALTA";
            titulo = "Entrar em contato hoje";
            motivo = "Risco alto: " + resumoSinais(risco);
        } else if (risco.diasSemContato() >= 30) {
            prioridade = "MÉDIA";
            titulo = "Retomar relacionamento";
            motivo = "Há " + risco.diasSemContato() + " dias sem contato registrado.";
        } else {
            prioridade = "MÉDIA";
            titulo = "Acompanhar evolução";
            motivo = "Foram identificados sinais que merecem acompanhamento: " + resumoSinais(risco);
        }

        RecomendacaoAdaptativaService.Recomendacao adaptativa = recomendacaoAdaptativa.recomendar(personalId, risco);
        return new AcaoAssistenteView(
                risco, prioridade, titulo, motivo, retencao.gerarMensagem(risco),
                risco.telefone() != null && !risco.telefone().isBlank(),
                adaptativa.acao(), adaptativa.justificativa(), adaptativa.confianca()
        );
    }

    private String resumoSinais(ScoreRiscoAlunoView risco) {
        if (risco.sinais() == null || risco.sinais().isEmpty()) {
            return "o score de risco indica necessidade de acompanhamento";
        }
        return String.join(" · ", risco.sinais().stream().limit(3).toList());
    }
}
