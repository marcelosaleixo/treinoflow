package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.AnaliseTendenciaProgressaoView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.ExecucaoSerie;
import com.marceloaleixo.treinoflow.entity.TreinoExercicio;
import com.marceloaleixo.treinoflow.repository.ExecucaoSerieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Etapa 85 — IA de Progressão Baseada em Tendência.
 *
 * Analisa até seis sessões anteriores do mesmo exercício e combina:
 * volume, repetições, RPE, consistência e alcance do topo da faixa.
 * O resultado é explicável e apenas recomendatório.
 */
@Service
public class TendenciaProgressaoService {
    private static final int MAX_SESSOES = 6;
    private static final Pattern NUMERO = Pattern.compile("(\\d+(?:[.,]\\d+)?)");
    private static final Pattern FAIXA = Pattern.compile("(\\d+)\\s*[-–aA]\\s*(\\d+)");

    private final ExecucaoSerieRepository repository;

    public TendenciaProgressaoService(ExecucaoSerieRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public AnaliseTendenciaProgressaoView analisar(TreinoExercicio item, Aluno aluno) {
        List<ExecucaoSerie> historico = repository.buscarHistoricoAnterior(
                item.getId(), aluno.getId(), LocalDate.now());

        Map<Long, List<ExecucaoSerie>> porSessao = new LinkedHashMap<>();
        for (ExecucaoSerie serie : historico) {
            if (serie.getRegistro() == null || serie.getRegistro().getId() == null || !serie.isConcluido()) continue;
            Long registroId = serie.getRegistro().getId();
            if (!porSessao.containsKey(registroId) && porSessao.size() >= MAX_SESSOES) break;
            porSessao.computeIfAbsent(registroId, k -> new ArrayList<>()).add(serie);
        }

        List<Sessao> sessoesRecentes = porSessao.values().stream()
                .map(this::resumir)
                .filter(Sessao::temDados)
                .toList();

        if (sessoesRecentes.isEmpty()) {
            return semDados();
        }

        // O repository retorna do mais recente para o mais antigo.
        Sessao maisRecente = sessoesRecentes.get(0);
        Sessao maisAntiga = sessoesRecentes.get(sessoesRecentes.size() - 1);

        double variacaoVolume = percentual(maisAntiga.volume(), maisRecente.volume());
        Double variacaoVolumeObj = maisAntiga.volume() != null && maisRecente.volume() != null
                ? variacaoVolume : null;
        Double variacaoRpe = maisAntiga.rpeMedio() != null && maisRecente.rpeMedio() != null
                ? maisRecente.rpeMedio() - maisAntiga.rpeMedio() : null;

        int[] faixa = faixaRepeticoes(item.getRepeticoes());
        int topoFaixa = 0;
        for (Sessao sessao : sessoesRecentes) {
            if (faixa[1] > 0 && sessao.repeticoesMaximas() >= faixa[1]) topoFaixa++;
        }

        boolean queda = variacaoVolumeObj != null && variacaoVolume <= -5.0
                || variacaoRpe != null && variacaoRpe >= 1.0;
        boolean melhora = (variacaoVolumeObj != null && variacaoVolume >= 5.0)
                || maisRecente.repeticoes() > maisAntiga.repeticoes();
        boolean esforcoControlado = maisRecente.rpeMedio() == null || maisRecente.rpeMedio() <= 8.5;
        boolean rpeControlado = variacaoRpe == null || variacaoRpe <= 0.75;

        double score = calcularScore(sessoesRecentes, variacaoVolumeObj, variacaoRpe, topoFaixa, faixa[1]);
        String scoreFormat = String.format(Locale.US, "%.0f", score);

        if (sessoesRecentes.size() >= 4 && queda) {
            return new AnaliseTendenciaProgressaoView(
                    "ATENCAO", "ALTA", "A tendência pede cautela",
                    "As últimas sessões mostram queda de volume/repetições ou aumento relevante do esforço (RPE).",
                    "Não aumente a carga com base nesta análise. Avalie recuperação, técnica, sono e consistência com o Personal.",
                    sessoesRecentes.size(), topoFaixa,
                    maisAntiga.repeticoes(), maisRecente.repeticoes(),
                    maisAntiga.volume(), maisRecente.volume(), variacaoVolumeObj,
                    maisAntiga.rpeMedio(), maisRecente.rpeMedio(), variacaoRpe, score);
        }

        if (sessoesRecentes.size() >= 4 && melhora && esforcoControlado && rpeControlado
                && (topoFaixa >= 3 || (variacaoVolumeObj != null && variacaoVolume >= 8))) {
            return new AnaliseTendenciaProgressaoView(
                    "PROGRESSAO", "ALTA", "Evidência consistente de progressão",
                    "O desempenho vem melhorando em várias sessões, com volume/repetições em alta e esforço ainda controlado.",
                    "Sugestão ao Personal: avaliar um pequeno aumento de carga ou outra progressão planejada na próxima prescrição.",
                    sessoesRecentes.size(), topoFaixa,
                    maisAntiga.repeticoes(), maisRecente.repeticoes(),
                    maisAntiga.volume(), maisRecente.volume(), variacaoVolumeObj,
                    maisAntiga.rpeMedio(), maisRecente.rpeMedio(), variacaoRpe, score);
        }

        if (sessoesRecentes.size() >= 3 && melhora && esforcoControlado && rpeControlado) {
            return new AnaliseTendenciaProgressaoView(
                    "PROGRESSAO", "MEDIA", "Tendência positiva",
                    "Há sinais de melhora ao comparar várias sessões, mas ainda falta evidência para uma recomendação de alta confiança.",
                    "Continue registrando as séries. Se a tendência permanecer, o Personal poderá avaliar progressão na próxima revisão.",
                    sessoesRecentes.size(), topoFaixa,
                    maisAntiga.repeticoes(), maisRecente.repeticoes(),
                    maisAntiga.volume(), maisRecente.volume(), variacaoVolumeObj,
                    maisAntiga.rpeMedio(), maisRecente.rpeMedio(), variacaoRpe, score);
        }

        if (sessoesRecentes.size() < 3) {
            return new AnaliseTendenciaProgressaoView(
                    "AGUARDAR", "BAIXA", "Mais histórico é necessário",
                    "Ainda há poucas sessões para separar uma evolução real de uma variação normal do treino.",
                    "Registre mais sessões com carga, repetições e RPE para aumentar a confiança da análise.",
                    sessoesRecentes.size(), topoFaixa,
                    maisAntiga.repeticoes(), maisRecente.repeticoes(),
                    maisAntiga.volume(), maisRecente.volume(), variacaoVolumeObj,
                    maisAntiga.rpeMedio(), maisRecente.rpeMedio(), variacaoRpe, score);
        }

        return new AnaliseTendenciaProgressaoView(
                "ESTAVEL", "MEDIA", "Tendência estável",
                "O desempenho não apresenta evidência consistente de progressão ou queda nas sessões analisadas.",
                "Mantenha a prescrição atual e continue registrando as séries para que a tendência fique mais clara.",
                sessoesRecentes.size(), topoFaixa,
                maisAntiga.repeticoes(), maisRecente.repeticoes(),
                maisAntiga.volume(), maisRecente.volume(), variacaoVolumeObj,
                maisAntiga.rpeMedio(), maisRecente.rpeMedio(), variacaoRpe, score);
    }

    private AnaliseTendenciaProgressaoView semDados() {
        return new AnaliseTendenciaProgressaoView(
                "SEM_DADOS", "BAIXA", "Ainda sem tendência",
                "Registre algumas sessões completas para o TreinoFlow identificar uma tendência real de desempenho.",
                "Comece pela prescrição do Personal e registre carga, repetições e RPE de cada série.",
                0, 0, null, null, null, null, null, null, null, null, null);
    }

    private Sessao resumir(List<ExecucaoSerie> lista) {
        lista.sort(Comparator.comparing(ExecucaoSerie::getNumeroSerie));
        int reps = lista.stream().map(ExecucaoSerie::getRepeticoesRealizadas)
                .filter(v -> v != null).mapToInt(Integer::intValue).sum();
        int maxReps = lista.stream().map(ExecucaoSerie::getRepeticoesRealizadas)
                .filter(v -> v != null).mapToInt(Integer::intValue).max().orElse(0);
        Double rpe = lista.stream().map(ExecucaoSerie::getRpe).filter(v -> v != null)
                .mapToInt(Integer::intValue).average().orElse(Double.NaN);
        Double volume = volume(lista);
        return new Sessao(reps, maxReps, Double.isNaN(rpe) ? null : rpe, volume);
    }

    private Double volume(List<ExecucaoSerie> lista) {
        BigDecimal total = BigDecimal.ZERO;
        boolean encontrou = false;
        for (ExecucaoSerie serie : lista) {
            BigDecimal carga = parseNumero(serie.getCargaRealizada());
            Integer reps = serie.getRepeticoesRealizadas();
            if (carga != null && reps != null) {
                total = total.add(carga.multiply(BigDecimal.valueOf(reps)));
                encontrou = true;
            }
        }
        return encontrou ? total.doubleValue() : null;
    }

    private BigDecimal parseNumero(String texto) {
        if (texto == null || texto.isBlank()) return null;
        Matcher m = NUMERO.matcher(texto.replace(',', '.'));
        if (!m.find()) return null;
        try { return new BigDecimal(m.group(1)); }
        catch (NumberFormatException ex) { return null; }
    }

    private double percentual(Double antigo, Double novo) {
        if (antigo == null || novo == null || antigo == 0) return 0;
        return ((novo - antigo) / antigo) * 100.0;
    }

    private double calcularScore(List<Sessao> sessoes, Double variacaoVolume, Double variacaoRpe,
                                 int topoFaixa, int maxFaixa) {
        if (sessoes.size() < 2) return 20;
        double score = 50;
        if (variacaoVolume != null) score += Math.max(-20, Math.min(25, variacaoVolume * 1.5));
        if (variacaoRpe != null) score -= Math.max(-10, Math.min(20, variacaoRpe * 8));
        if (maxFaixa > 0) score += Math.min(15, topoFaixa * 2.5);
        score += Math.min(10, sessoes.size() * 1.5);
        return Math.max(0, Math.min(100, score));
    }

    private int[] faixaRepeticoes(String valor) {
        if (valor == null || valor.isBlank()) return new int[]{0, 0};
        Matcher faixa = FAIXA.matcher(valor.trim());
        if (faixa.find()) return new int[]{Integer.parseInt(faixa.group(1)), Integer.parseInt(faixa.group(2))};
        Matcher numero = NUMERO.matcher(valor);
        if (numero.find()) {
            int n = (int) Double.parseDouble(numero.group(1).replace(',', '.'));
            return new int[]{n, n};
        }
        return new int[]{0, 0};
    }

    private record Sessao(int repeticoes, int repeticoesMaximas, Double rpeMedio, Double volume) {
        boolean temDados() { return repeticoes > 0 || volume != null || rpeMedio != null; }
    }
}
