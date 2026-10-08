package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.AnaliseMultiSerieView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.ExecucaoSerie;
import com.marceloaleixo.treinoflow.entity.RegistroTreinoAluno;
import com.marceloaleixo.treinoflow.entity.TreinoExercicio;
import com.marceloaleixo.treinoflow.repository.ExecucaoSerieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Etapa 84: analisa o comportamento das várias séries e das últimas sessões
 * do mesmo exercício. Não modifica carga prescrita nem executa alterações.
 */
@Service
public class AnaliseMultiSerieService {
    private static final int MAX_SESSOES = 6;
    private static final Pattern NUMERO_CARGA = Pattern.compile("(\\d+(?:[.,]\\d+)?)");

    private final ExecucaoSerieRepository series;

    public AnaliseMultiSerieService(ExecucaoSerieRepository series) {
        this.series = series;
    }

    @Transactional(readOnly = true)
    public AnaliseMultiSerieView analisar(TreinoExercicio item, Aluno aluno) {
        List<ExecucaoSerie> historico = series.buscarHistoricoAnterior(item.getId(), aluno.getId(), LocalDate.now());
        if (historico.isEmpty()) {
            return new AnaliseMultiSerieView(
                    "SEM_DADOS", "Ainda sem histórico suficiente",
                    "Registre as séries deste exercício para o TreinoFlow analisar desempenho, volume e fadiga.",
                    "A análise começa automaticamente depois da primeira sessão registrada.",
                    0, 0, 0, null, null, null, null, null, null);
        }

        Map<Long, List<ExecucaoSerie>> porSessao = new LinkedHashMap<>();
        for (ExecucaoSerie serie : historico) {
            if (serie.getRegistro() == null || serie.getRegistro().getId() == null || !serie.isConcluido()) continue;
            Long registroId = serie.getRegistro().getId();
            if (!porSessao.containsKey(registroId) && porSessao.size() >= MAX_SESSOES) break;
            porSessao.computeIfAbsent(registroId, k -> new ArrayList<>()).add(serie);
        }

        List<Sessao> sessoes = porSessao.values().stream().map(this::resumir).toList();
        if (sessoes.isEmpty()) {
            return new AnaliseMultiSerieView(
                    "SEM_DADOS", "Sem séries concluídas",
                    "Não há séries concluídas suficientes para uma análise confiável.",
                    "Registre a execução completa do exercício para gerar o histórico.",
                    0, 0, 0, null, null, null, null, null, null);
        }

        Sessao atual = sessoes.get(0);
        Sessao anterior = sessoes.size() > 1 ? sessoes.get(1) : null;
        Double variacaoVolume = variacao(atual.volume(), anterior == null ? null : anterior.volume());

        String nivel;
        String titulo;
        String resumo;
        String detalhe;

        double quedaReps = anterior == null ? 0 : percentual(anterior.repeticoes(), atual.repeticoes());
        double aumentoRpe = anterior == null || anterior.rpeMedio() == null || atual.rpeMedio() == null
                ? 0 : atual.rpeMedio() - anterior.rpeMedio();

        if (atual.rpeMedio() != null && atual.rpeMedio() >= 9.0) {
            nivel = "ATENCAO";
            titulo = "Esforço alto no conjunto";
            resumo = String.format("A média de RPE desta sessão foi %.1f. O conjunto sugere atenção à fadiga.", atual.rpeMedio());
            detalhe = "Mantenha a prescrição e priorize técnica, recuperação e execução consistente antes de aumentar a carga.";
        } else if (anterior != null && atual.repeticoes() > anterior.repeticoes()
                && (atual.rpeMedio() == null || atual.rpeMedio() <= 8.5)
                && (variacaoVolume == null || variacaoVolume >= 5)) {
            nivel = "PROGRESSAO";
            titulo = "Tendência de progressão";
            resumo = "O conjunto atual apresentou mais repetições e/ou volume que a sessão anterior sem aumento relevante de esforço.";
            detalhe = "Existe sinal positivo de desempenho. A decisão de aumentar carga continua sendo do Personal.";
        } else if (anterior != null && quedaReps >= 10 && aumentoRpe >= 0.5) {
            nivel = "QUEDA";
            titulo = "Queda de desempenho";
            resumo = String.format("As repetições caíram %.0f%% enquanto o RPE médio aumentou %.1f ponto(s).", quedaReps, aumentoRpe);
            detalhe = "Evite progressão agora. Avalie descanso, recuperação, técnica e consistência das séries.";
        } else if (atual.rpeMedioPrimeiraSerie() != null && atual.rpeMedioUltimaSerie() != null
                && atual.rpeMedioUltimaSerie() - atual.rpeMedioPrimeiraSerie() >= 2.0) {
            nivel = "FADIGA";
            titulo = "Fadiga crescente dentro da sessão";
            resumo = String.format("O RPE subiu de %.1f para %.1f entre a primeira e a última série.",
                    atual.rpeMedioPrimeiraSerie(), atual.rpeMedioUltimaSerie());
            detalhe = "Observe a qualidade das últimas séries e mantenha o descanso prescrito antes de considerar progressão.";
        } else {
            nivel = "ESTAVEL";
            titulo = "Desempenho estável";
            resumo = anterior == null
                    ? "Esta é a primeira sessão usada como referência multi-série."
                    : "O conjunto permanece dentro de uma faixa estável de desempenho.";
            detalhe = "Continue registrando carga, repetições e RPE para que o TreinoFlow consiga identificar uma tendência com mais confiança.";
        }

        return new AnaliseMultiSerieView(
                nivel, titulo, resumo, detalhe,
                sessoes.size(), atual.series(), atual.repeticoes(), atual.volume(),
                anterior == null ? null : anterior.volume(), variacaoVolume,
                atual.rpeMedio(), atual.rpeMedioPrimeiraSerie(), atual.rpeMedioUltimaSerie());
    }

    private Sessao resumir(List<ExecucaoSerie> lista) {
        lista.sort((a, b) -> Integer.compare(a.getNumeroSerie(), b.getNumeroSerie()));
        int reps = lista.stream().map(ExecucaoSerie::getRepeticoesRealizadas).filter(v -> v != null).mapToInt(Integer::intValue).sum();
        Double rpe = mediaRpe(lista);
        Double volume = volume(lista);
        Double primeiraRpe = lista.isEmpty() ? null : lista.get(0).getRpe() == null ? null : lista.get(0).getRpe().doubleValue();
        Double ultimaRpe = lista.isEmpty() ? null : lista.get(lista.size() - 1).getRpe() == null ? null : lista.get(lista.size() - 1).getRpe().doubleValue();
        return new Sessao(lista.size(), reps, rpe, volume, primeiraRpe, ultimaRpe);
    }

    private Double mediaRpe(List<ExecucaoSerie> lista) {
        List<Integer> valores = lista.stream().map(ExecucaoSerie::getRpe).filter(v -> v != null).toList();
        if (valores.isEmpty()) return null;
        return valores.stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    private Double volume(List<ExecucaoSerie> lista) {
        BigDecimal total = BigDecimal.ZERO;
        boolean encontrouCarga = false;
        for (ExecucaoSerie serie : lista) {
            BigDecimal carga = parseCarga(serie.getCargaRealizada());
            Integer reps = serie.getRepeticoesRealizadas();
            if (carga != null && reps != null) {
                total = total.add(carga.multiply(BigDecimal.valueOf(reps)));
                encontrouCarga = true;
            }
        }
        return encontrouCarga ? total.doubleValue() : null;
    }

    private BigDecimal parseCarga(String texto) {
        if (texto == null || texto.isBlank()) return null;
        Matcher matcher = NUMERO_CARGA.matcher(texto);
        if (!matcher.find()) return null;
        try {
            return new BigDecimal(matcher.group(1).replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Double variacao(Double atual, Double anterior) {
        if (atual == null || anterior == null || anterior == 0) return null;
        return ((atual - anterior) / anterior) * 100.0;
    }

    private double percentual(int anterior, int atual) {
        if (anterior <= 0) return 0;
        return ((anterior - atual) * 100.0) / anterior;
    }

    private record Sessao(int series, int repeticoes, Double rpeMedio, Double volume,
                          Double rpeMedioPrimeiraSerie, Double rpeMedioUltimaSerie) {}
}
