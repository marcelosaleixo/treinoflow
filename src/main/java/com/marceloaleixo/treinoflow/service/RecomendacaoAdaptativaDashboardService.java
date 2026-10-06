package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.RecomendacaoAdaptativaDashboardView;
import com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecomendacaoAdaptativaDashboardService {
    private final ScoreRiscoAlunoService scoreRisco;
    private final RecomendacaoAdaptativaService adaptativa;

    public RecomendacaoAdaptativaDashboardService(ScoreRiscoAlunoService scoreRisco,
                                                  RecomendacaoAdaptativaService adaptativa) {
        this.scoreRisco = scoreRisco;
        this.adaptativa = adaptativa;
    }

    public RecomendacaoAdaptativaDashboardView dashboard(Long personalId) {
        List<ScoreRiscoAlunoView> riscos = scoreRisco.listar(personalId).stream()
                .filter(r -> r.score() >= 25).toList();
        List<RecomendacaoAdaptativaDashboardView.Item> itens = new ArrayList<>();
        long alta = 0, baixa = 0;
        for (ScoreRiscoAlunoView risco : riscos) {
            var r = adaptativa.recomendar(personalId, risco);
            if ("ALTA".equals(r.confianca())) alta++; else baixa++;
            itens.add(new RecomendacaoAdaptativaDashboardView.Item(r.nivel(risco.score()), r.acao(), r.confianca(), r.justificativa(), 1));
        }
        String resumo = riscos.isEmpty()
                ? "Nenhum aluno está em faixa de risco que exija recomendação adaptativa."
                : alta > 0
                ? "Há recomendações baseadas em resultados reais da sua carteira. Priorize os casos com confiança alta."
                : "Ainda não há amostra suficiente para recomendações fortes; continue registrando os resultados das ações.";
        return new RecomendacaoAdaptativaDashboardView("Histórico dos últimos 90 dias", riscos.size(), alta, baixa, resumo, itens);
    }
}
