package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.OtimizacaoRetencaoDashboardView;
import com.marceloaleixo.treinoflow.entity.AcaoAssistente;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente;
import com.marceloaleixo.treinoflow.repository.AcaoAssistenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OtimizacaoRetencaoService {
    private static final int DIAS_HISTORICO = 180;
    private static final int MIN_AMOSTRA = 3;
    private final AcaoAssistenteRepository repository;

    public OtimizacaoRetencaoService(AcaoAssistenteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public OtimizacaoRetencaoDashboardView dashboard(Long personalId) {
        LocalDateTime inicio = LocalDateTime.now().minusDays(DIAS_HISTORICO);
        List<AcaoAssistente> acoes = repository.buscarDesde(personalId, inicio);
        List<AcaoAssistente> finais = acoes.stream().filter(a -> resultadoFinal(a.getResultado())).toList();
        long sucessos = finais.stream().filter(this::sucesso).count();

        List<OtimizacaoRetencaoDashboardView.EstrategiaView> estrategias = new ArrayList<>();
        for (String faixa : List.of("CRÍTICO", "ALTO", "MÉDIO")) {
            Map<TipoAcaoAssistente, List<AcaoAssistente>> grupos = finais.stream()
                    .filter(a -> faixa(a.getScoreRisco()).equals(faixa))
                    .filter(a -> a.getTipoAcao() != null)
                    .collect(Collectors.groupingBy(AcaoAssistente::getTipoAcao));
            grupos.entrySet().stream()
                    .filter(e -> e.getValue().size() >= MIN_AMOSTRA)
                    .map(e -> estrategia(faixa, e.getKey(), e.getValue()))
                    .max(Comparator.comparingDouble(OtimizacaoRetencaoDashboardView.EstrategiaView::taxa)
                            .thenComparingLong(OtimizacaoRetencaoDashboardView.EstrategiaView::amostra))
                    .ifPresent(estrategias::add);
        }

        List<OtimizacaoRetencaoDashboardView.HorarioView> horarios = new ArrayList<>();
        for (String faixa : List.of("CRÍTICO", "ALTO", "MÉDIO")) {
            finais.stream().filter(a -> faixa(a.getScoreRisco()).equals(faixa))
                    .collect(Collectors.groupingBy(a -> a.getExecutadaEm().getHour()))
                    .entrySet().stream()
                    .filter(e -> e.getValue().size() >= MIN_AMOSTRA)
                    .map(e -> horario(faixa, e.getKey(), e.getValue()))
                    .max(Comparator.comparingDouble(OtimizacaoRetencaoDashboardView.HorarioView::taxa)
                            .thenComparingLong(OtimizacaoRetencaoDashboardView.HorarioView::amostra))
                    .ifPresent(horarios::add);
        }

        String resumo = estrategias.isEmpty()
                ? "Ainda não há histórico suficiente para uma recomendação estatística confiável."
                : "O TreinoFlow encontrou uma estratégia vencedora por faixa de risco usando apenas resultados finais do seu histórico.";

        double taxa = finais.isEmpty() ? 0D : sucessos * 100D / finais.size();
        return new OtimizacaoRetencaoDashboardView(
                "Últimos " + DIAS_HISTORICO + " dias", acoes.size(), finais.size(), sucessos, taxa,
                estrategias, horarios, resumo);
    }

    /** Retorna o horário com melhor taxa de recuperação/renovação para a faixa de risco.
     *  Só recomenda um horário quando existem pelo menos 3 resultados finais naquela hora.
     */
    @Transactional(readOnly = true)
    public HorarioRecomendado melhorHorarioPara(int score, Long personalId) {
        String faixa = faixa(score);
        List<AcaoAssistente> finais = repository.buscarDesde(personalId, LocalDateTime.now().minusDays(DIAS_HISTORICO)).stream()
                .filter(a -> faixa(a.getScoreRisco()).equals(faixa))
                .filter(a -> a.getExecutadaEm() != null && resultadoFinal(a.getResultado()))
                .toList();

        return finais.stream()
                .collect(Collectors.groupingBy(a -> a.getExecutadaEm().getHour()))
                .entrySet().stream()
                .filter(e -> e.getValue().size() >= MIN_AMOSTRA)
                .map(e -> {
                    long sucessos = e.getValue().stream().filter(this::sucesso).count();
                    double taxa = sucessos * 100D / e.getValue().size();
                    String confianca = e.getValue().size() >= 10 ? "ALTA" : "MÉDIA";
                    return new HorarioRecomendado(e.getKey(), e.getValue().size(), sucessos, taxa, confianca);
                })
                .max(Comparator.comparingDouble(HorarioRecomendado::taxa)
                        .thenComparingLong(HorarioRecomendado::amostra)
                        .thenComparingInt(HorarioRecomendado::hora))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public OtimizacaoRetencaoDashboardView.EstrategiaView estrategiaPara(int score, Long personalId) {
        String faixa = faixa(score);
        List<AcaoAssistente> finais = repository.buscarDesde(personalId, LocalDateTime.now().minusDays(DIAS_HISTORICO)).stream()
                .filter(a -> faixa(a.getScoreRisco()).equals(faixa))
                .filter(a -> a.getTipoAcao() != null && resultadoFinal(a.getResultado()))
                .toList();
        return finais.stream().collect(Collectors.groupingBy(AcaoAssistente::getTipoAcao)).entrySet().stream()
                .filter(e -> e.getValue().size() >= MIN_AMOSTRA)
                .map(e -> estrategia(faixa, e.getKey(), e.getValue()))
                .max(Comparator.comparingDouble(OtimizacaoRetencaoDashboardView.EstrategiaView::taxa)
                        .thenComparingLong(OtimizacaoRetencaoDashboardView.EstrategiaView::amostra))
                .orElse(null);
    }

    private OtimizacaoRetencaoDashboardView.EstrategiaView estrategia(String faixa, TipoAcaoAssistente tipo, List<AcaoAssistente> grupo) {
        long sucessos = grupo.stream().filter(this::sucesso).count();
        double taxa = sucessos * 100D / grupo.size();
        String confianca = grupo.size() >= 10 ? "ALTA" : "MÉDIA";
        return new OtimizacaoRetencaoDashboardView.EstrategiaView(
                faixa, tipo.getDescricao(), grupo.size(), sucessos, taxa, confianca,
                "Baseada em " + grupo.size() + " resultados finais da faixa " + faixa + ".");
    }

    private OtimizacaoRetencaoDashboardView.HorarioView horario(String faixa, int hora, List<AcaoAssistente> grupo) {
        long sucessos = grupo.stream().filter(this::sucesso).count();
        double taxa = sucessos * 100D / grupo.size();
        return new OtimizacaoRetencaoDashboardView.HorarioView(faixa, hora, grupo.size(), sucessos, taxa,
                grupo.size() >= 10 ? "ALTA" : "MÉDIA");
    }

    private boolean sucesso(AcaoAssistente a) {
        return a.getResultado() == ResultadoCrm.RECUPERADO || a.getResultado() == ResultadoCrm.RENOVADO;
    }

    public record HorarioRecomendado(int hora, int amostra, long sucessos, double taxa, String confianca) {}

    private boolean resultadoFinal(ResultadoCrm r) {
        return r == ResultadoCrm.RECUPERADO || r == ResultadoCrm.RENOVADO ||
                r == ResultadoCrm.CANCELAMENTO || r == ResultadoCrm.SEM_RESPOSTA || r == ResultadoCrm.OUTRO;
    }

    private String faixa(int score) {
        if (score >= 75) return "CRÍTICO";
        if (score >= 50) return "ALTO";
        return "MÉDIO";
    }
}
