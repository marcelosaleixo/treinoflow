package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView;
import com.marceloaleixo.treinoflow.entity.AcaoAssistente;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente;
import com.marceloaleixo.treinoflow.repository.AcaoAssistenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class RecomendacaoAdaptativaService {
    private static final int DIAS_HISTORICO = 90;
    private static final int MIN_RESULTADOS = 3;
    private final AcaoAssistenteRepository repository;
    private final OtimizacaoRetencaoService otimizacao;

    public RecomendacaoAdaptativaService(AcaoAssistenteRepository repository, OtimizacaoRetencaoService otimizacao) {
        this.repository = repository;
        this.otimizacao = otimizacao;
    }

    @Transactional(readOnly = true)
    public Recomendacao recomendar(Long personalId, ScoreRiscoAlunoView risco) {
        List<AcaoAssistente> acoes = repository.buscarDesde(personalId, LocalDateTime.now().minusDays(DIAS_HISTORICO)).stream()
                .filter(a -> faixa(a.getScoreRisco()).equals(faixa(risco.score())))
                .toList();

        List<Desempenho> desempenhos = List.of(TipoAcaoAssistente.values()).stream()
                .map(tipo -> desempenho(acoes, tipo))
                .filter(d -> d.resultadosFinais() >= MIN_RESULTADOS)
                .sorted(Comparator.comparingDouble(Desempenho::taxaSucesso).reversed()
                        .thenComparing(Desempenho::resultadosFinais, Comparator.reverseOrder()))
                .toList();

        if (!desempenhos.isEmpty()) {
            Desempenho melhor = desempenhos.get(0);
            return new Recomendacao(melhor.tipo(),
                    "Nos últimos " + DIAS_HISTORICO + " dias, esta ação teve " +
                            String.format(java.util.Locale.US, "%.1f", melhor.taxaSucesso()) +
                            "% de recuperação/renovação em casos da mesma faixa de risco, com " +
                            melhor.resultadosFinais() + " resultados finais.",
                    "ALTA");
        }

        var aprendida = otimizacao.estrategiaPara(risco.score(), personalId);
        if (aprendida != null) {
            return new Recomendacao(aprendida.acao(),
                    "Estratégia aprendida com " + aprendida.amostra() + " resultados finais da mesma faixa de risco, com taxa de sucesso de " +
                            String.format(java.util.Locale.US, "%.1f", aprendida.taxa()) + "%.",
                    aprendida.confianca());
        }

        TipoAcaoAssistente padrao = risco.telefone() != null && !risco.telefone().isBlank()
                ? TipoAcaoAssistente.WHATSAPP : TipoAcaoAssistente.FOLLOW_UP;
        return new Recomendacao(padrao.getDescricao(),
                "Ainda não há histórico suficiente para uma estratégia aprendida. A recomendação usa o canal disponível.",
                "BAIXA");
    }

    private Desempenho desempenho(List<AcaoAssistente> acoes, TipoAcaoAssistente tipo) {
        List<AcaoAssistente> grupo = acoes.stream().filter(a -> a.getTipoAcao() == tipo).toList();
        long finais = grupo.stream().filter(a -> resultadoFinal(a.getResultado())).count();
        long sucesso = grupo.stream().filter(a -> a.getResultado() == ResultadoCrm.RECUPERADO || a.getResultado() == ResultadoCrm.RENOVADO).count();
        double taxa = finais == 0 ? 0D : sucesso * 100D / finais;
        return new Desempenho(tipo.getDescricao(), finais, taxa);
    }

    private boolean resultadoFinal(ResultadoCrm r) {
        return r == ResultadoCrm.RECUPERADO || r == ResultadoCrm.RENOVADO ||
                r == ResultadoCrm.CANCELAMENTO || r == ResultadoCrm.SEM_RESPOSTA || r == ResultadoCrm.OUTRO;
    }

    private String faixa(int score) {
        if (score >= 75) return "CRÍTICO";
        if (score >= 50) return "ALTO";
        return "MÉDIO";
    }

    public record Recomendacao(String acao, String justificativa, String confianca) {
        public String nivel(int score) {
            if (score >= 75) return "CRÍTICO";
            if (score >= 50) return "ALTO";
            return "MÉDIO";
        }
    }
    private record Desempenho(String tipo, long resultadosFinais, double taxaSucesso) {}
}
