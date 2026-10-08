package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.CentralInteligenciaAcaoView;
import com.marceloaleixo.treinoflow.dto.CentralInteligenciaDashboardView;
import com.marceloaleixo.treinoflow.dto.GamificacaoDashboardView;
import com.marceloaleixo.treinoflow.dto.MetaComercialDashboardView;
import com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView;
import com.marceloaleixo.treinoflow.dto.TendenciaPerformanceDashboardView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CentralInteligenciaService {
    private final MetaComercialService metas;
    private final GamificacaoService gamificacao;
    private final TendenciaPerformanceService tendencia;
    private final ScoreRiscoAlunoService risco;

    public CentralInteligenciaService(MetaComercialService metas,
                                      GamificacaoService gamificacao,
                                      TendenciaPerformanceService tendencia,
                                      ScoreRiscoAlunoService risco) {
        this.metas = metas;
        this.gamificacao = gamificacao;
        this.tendencia = tendencia;
        this.risco = risco;
    }

    @Transactional(readOnly = true)
    public CentralInteligenciaDashboardView dashboard(Long personalId) {
        MetaComercialDashboardView meta = metas.dashboard(personalId);
        GamificacaoDashboardView pontos = gamificacao.dashboard(personalId);
        TendenciaPerformanceDashboardView previsao = tendencia.dashboard(personalId);

        List<ScoreRiscoAlunoView> riscos = risco.listar(personalId);
        List<ScoreRiscoAlunoView> prioritarios = riscos.stream()
                .filter(r -> r.score() >= 50)
                .sorted(Comparator.comparingInt(ScoreRiscoAlunoView::score).reversed())
                .limit(8)
                .toList();
        List<ScoreRiscoAlunoView> semContato = riscos.stream()
                .filter(r -> r.diasSemContato() >= 30)
                .sorted(Comparator.comparingInt(ScoreRiscoAlunoView::score).reversed())
                .limit(8)
                .toList();

        List<CentralInteligenciaAcaoView> acoes = gerarAcoes(meta, previsao, prioritarios, semContato);
        long criticos = riscos.stream().filter(r -> "CRITICO".equals(r.nivel())).count();
        long altos = riscos.stream().filter(r -> "ALTO".equals(r.nivel())).count();
        long semContato30 = riscos.stream().filter(r -> r.diasSemContato() >= 30).count();
        String prioridade = prioridade(criticos, altos, meta.progressoGeral(), previsao.tendencia());
        String resumo = resumo(criticos, altos, meta, previsao);

        return new CentralInteligenciaDashboardView(
                meta, pontos, previsao, prioritarios, semContato, acoes,
                criticos, altos, semContato30, pontos.pontos(), previsao.projecaoProximoMes(),
                previsao.projecaoNivel(), prioridade, resumo
        );
    }

    private List<CentralInteligenciaAcaoView> gerarAcoes(MetaComercialDashboardView meta,
                                                         TendenciaPerformanceDashboardView previsao,
                                                         List<ScoreRiscoAlunoView> prioritarios,
                                                         List<ScoreRiscoAlunoView> semContato) {
        List<CentralInteligenciaAcaoView> acoes = new ArrayList<>();
        if (!prioritarios.isEmpty()) {
            acoes.add(new CentralInteligenciaAcaoView(
                    "ALTA", "Recuperar alunos em risco",
                    prioritarios.size() + " aluno(s) estão com risco alto ou crítico. Comece pelos maiores scores.",
                    "/retencao/central", "Abrir Central de Recuperação"));
        }
        if (!semContato.isEmpty()) {
            acoes.add(new CentralInteligenciaAcaoView(
                    "ALTA", "Retomar contato com a carteira",
                    semContato.size() + " aluno(s) estão sem contato registrado há 30 dias ou mais.",
                    "/crm", "Abrir CRM"));
        }
        if (meta.percentualTreinos() < 75) {
            acoes.add(new CentralInteligenciaAcaoView(
                    "MEDIA", "Aumentar execução de treinos",
                    "Treinos estão em " + meta.percentualTreinos() + "% da meta do mês. Priorize acompanhamento e registro das execuções.",
                    "/performance", "Abrir Performance"));
        }
        if (meta.percentualRecuperacoes() < 75) {
            acoes.add(new CentralInteligenciaAcaoView(
                    "MEDIA", "Acelerar recuperação",
                    "Recuperações estão em " + meta.percentualRecuperacoes() + "% da meta do mês.",
                    "/retencao/central", "Ações de Retenção"));
        }
        if (previsao.variacaoMediaPontos() < 0) {
            acoes.add(new CentralInteligenciaAcaoView(
                    "ALTA", "Reverter tendência negativa",
                    "A projeção indica queda média de " + Math.abs(previsao.variacaoMediaPontos()) + " pontos por mês.",
                    "/gamificacao/tendencias", "Ver Tendências"));
        }
        if (acoes.isEmpty()) {
            acoes.add(new CentralInteligenciaAcaoView(
                    "BAIXA", "Manter consistência",
                    "Os principais indicadores estão sob controle. Continue acompanhando metas, retenção e evolução.",
                    "/gamificacao/tendencias", "Acompanhar evolução"));
        }
        return acoes.stream().limit(5).toList();
    }

    private String prioridade(long criticos, long altos, int progresso, String tendencia) {
        if (criticos > 0 || "NEGATIVA".equals(tendencia)) return "ALTA";
        if (altos > 0 || progresso < 75) return "MEDIA";
        return "BAIXA";
    }

    private String resumo(long criticos, long altos, MetaComercialDashboardView meta,
                          TendenciaPerformanceDashboardView tendencia) {
        if (criticos > 0) {
            return "Há " + criticos + " aluno(s) em risco crítico. A prioridade de hoje é retenção.";
        }
        if ("NEGATIVA".equals(tendencia.tendencia())) {
            return "Sua performance está em tendência negativa. Use as ações sugeridas para recuperar ritmo.";
        }
        if (meta.progressoGeral() < 75) {
            return "O mês está abaixo de 75% da meta geral. Concentre esforço no indicador mais distante.";
        }
        return "Sua operação está em um cenário controlado. Use a central para antecipar riscos e manter consistência.";
    }
}
