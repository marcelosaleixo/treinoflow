package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.HistoricoPerformanceDashboardView;
import com.marceloaleixo.treinoflow.dto.PerformanceMensalView;
import com.marceloaleixo.treinoflow.dto.TendenciaPerformanceDashboardView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TendenciaPerformanceService {
    private final HistoricoPerformanceService historicoPerformanceService;

    public TendenciaPerformanceService(HistoricoPerformanceService historicoPerformanceService) {
        this.historicoPerformanceService = historicoPerformanceService;
    }

    @Transactional(readOnly = true)
    public TendenciaPerformanceDashboardView dashboard(Long personalId) {
        HistoricoPerformanceDashboardView historico = historicoPerformanceService.dashboard(personalId);
        List<PerformanceMensalView> meses = historico.meses();
        PerformanceMensalView atual = historico.atual();

        int media = (int) Math.round(meses.stream().mapToInt(PerformanceMensalView::pontos).average().orElse(0));
        int variacaoMedia = calcularVariacaoMedia(meses);
        int projecao = Math.max(0, atual.pontos() + variacaoMedia);
        String nivel = nivel(projecao);
        String tendencia = classificarTendencia(variacaoMedia);
        String descricao = descricaoTendencia(tendencia, variacaoMedia);
        String alerta = gerarAlerta(meses, variacaoMedia);
        String recomendacao = gerarRecomendacao(atual, variacaoMedia);

        return new TendenciaPerformanceDashboardView(
                tendencia, descricao, variacaoMedia, projecao, nivel, media,
                alerta, recomendacao, atual, meses
        );
    }

    private int calcularVariacaoMedia(List<PerformanceMensalView> meses) {
        if (meses.size() < 2) return 0;
        int inicio = Math.max(1, meses.size() - 3);
        int soma = 0;
        int quantidade = 0;
        for (int i = inicio; i < meses.size(); i++) {
            soma += meses.get(i).pontos() - meses.get(i - 1).pontos();
            quantidade++;
        }
        return quantidade == 0 ? 0 : Math.round((float) soma / quantidade);
    }

    private String classificarTendencia(int variacao) {
        if (variacao >= 15) return "POSITIVA";
        if (variacao <= -15) return "NEGATIVA";
        return "ESTÁVEL";
    }

    private String descricaoTendencia(String tendencia, int variacao) {
        return switch (tendencia) {
            case "POSITIVA" -> "Sua pontuação vem crescendo em média " + variacao + " pontos por mês.";
            case "NEGATIVA" -> "Sua pontuação vem caindo em média " + Math.abs(variacao) + " pontos por mês.";
            default -> "Sua pontuação está relativamente estável nos últimos meses.";
        };
    }

    private String gerarAlerta(List<PerformanceMensalView> meses, int variacao) {
        if (variacao <= -15) return "Atenção: a tendência de performance está negativa. Revise o indicador que mais perdeu ritmo.";
        if (meses.size() >= 3) {
            PerformanceMensalView a = meses.get(meses.size() - 1);
            PerformanceMensalView b = meses.get(meses.size() - 2);
            PerformanceMensalView c = meses.get(meses.size() - 3);
            if (a.recuperacoes() < b.recuperacoes() && b.recuperacoes() < c.recuperacoes()) {
                return "Atenção: as recuperações estão caindo pelo terceiro mês consecutivo.";
            }
            if (a.treinosRealizados() < b.treinosRealizados() && b.treinosRealizados() < c.treinosRealizados()) {
                return "Atenção: os treinos concluídos estão caindo pelo terceiro mês consecutivo.";
            }
        }
        if (variacao >= 15) return "Tendência positiva: mantenha o ritmo e concentre esforço no indicador mais distante da meta.";
        return "Sem alerta crítico no momento. Continue acompanhando a evolução mensal.";
    }

    private String gerarRecomendacao(PerformanceMensalView atual, int variacao) {
        if (variacao < 0) {
            if (atual.recuperacoes() == 0) return "Priorize ações de recuperação e follow-ups com alunos em risco.";
            if (atual.treinosRealizados() == 0) return "Aumente o acompanhamento dos treinos e registre as execuções concluídas.";
            return "Revise a meta com menor progresso e transforme-a em prioridade da próxima semana.";
        }
        if (atual.progressoGeral() < 75) return "Concentre a próxima semana no indicador mais distante de 75% da meta.";
        if (atual.progressoGeral() < 100) return "Você está perto da meta. Foque no indicador mais próximo de completar 100%.";
        return "Mantenha a consistência e busque o próximo nível de performance.";
    }

    private String nivel(int pontos) {
        if (pontos >= 500) return "Elite";
        if (pontos >= 250) return "Alta performance";
        if (pontos >= 100) return "Em evolução";
        return "Iniciante";
    }
}
