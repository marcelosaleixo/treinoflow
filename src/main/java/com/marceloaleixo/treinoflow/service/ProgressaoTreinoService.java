package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.ProgressaoTreinoView;
import com.marceloaleixo.treinoflow.entity.ExecucaoExercicio;
import com.marceloaleixo.treinoflow.repository.ExecucaoExercicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProgressaoTreinoService {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern NUMERO = Pattern.compile("(\\d+(?:[.,]\\d+)?)");

    private final ExecucaoExercicioRepository execucoes;

    public ProgressaoTreinoService(ExecucaoExercicioRepository execucoes) {
        this.execucoes = execucoes;
    }

    @Transactional(readOnly = true)
    public ProgressaoTreinoView montar(Long personalId) {
        LocalDate inicio = LocalDate.now().minusDays(29);
        List<ExecucaoExercicio> registros = execucoes.buscarDoPersonalDesde(personalId, inicio).stream()
                .filter(ExecucaoExercicio::isConcluido)
                .sorted(Comparator
                        .comparing((ExecucaoExercicio e) -> e.getRegistro().getDataExecucao(), Comparator.reverseOrder())
                        .thenComparing(e -> e.getDataRegistro(), Comparator.reverseOrder()))
                .toList();

        Map<String, List<ExecucaoExercicio>> historicos = new LinkedHashMap<>();
        for (ExecucaoExercicio execucao : registros) {
            Long alunoId = execucao.getRegistro().getAluno().getId();
            Long exercicioId = execucao.getTreinoExercicio().getExercicio().getId();
            String chave = alunoId + ":" + exercicioId;
            List<ExecucaoExercicio> historico = historicos.computeIfAbsent(chave, k -> new ArrayList<>());
            if (historico.size() < 2) {
                historico.add(execucao);
            }
        }

        List<ProgressaoTreinoView.ItemProgressaoView> itens = new ArrayList<>();
        for (List<ExecucaoExercicio> historico : historicos.values()) {
            ExecucaoExercicio atual = historico.get(0);
            ExecucaoExercicio anterior = historico.size() > 1 ? historico.get(1) : null;
            itens.add(criarItem(atual, anterior));
        }

        itens.sort(Comparator
                .comparingInt((ProgressaoTreinoView.ItemProgressaoView i) -> prioridade(i.status()))
                .thenComparing(ProgressaoTreinoView.ItemProgressaoView::alunoNome)
                .thenComparing(ProgressaoTreinoView.ItemProgressaoView::exercicioNome));

        long emProgressao = itens.stream().filter(i -> "PROGRESSO".equals(i.status())).count();
        long estaveis = itens.stream().filter(i -> "ESTAVEL".equals(i.status())).count();
        long emQueda = itens.stream().filter(i -> "QUEDA".equals(i.status())).count();
        long semComparacao = itens.stream().filter(i -> "SEM_COMPARACAO".equals(i.status())).count();

        return new ProgressaoTreinoView(itens.size(), emProgressao, estaveis, emQueda, semComparacao, itens);
    }

    private ProgressaoTreinoView.ItemProgressaoView criarItem(ExecucaoExercicio atual, ExecucaoExercicio anterior) {
        String cargaAtual = texto(atual.getCargaRealizada());
        String cargaAnterior = anterior == null ? null : texto(anterior.getCargaRealizada());
        Double valorAtual = extrairNumero(cargaAtual);
        Double valorAnterior = extrairNumero(cargaAnterior);

        String status;
        String label;
        String sugestao;
        double variacao = 0D;

        if (valorAtual == null || valorAnterior == null || valorAnterior <= 0D) {
            status = "SEM_COMPARACAO";
            label = "Sem comparação";
            sugestao = "A carga não permite comparação numérica confiável. Acompanhe carga, repetições e técnica manualmente.";
        } else {
            variacao = ((valorAtual - valorAnterior) / valorAnterior) * 100D;
            if (variacao >= 2D) {
                status = "PROGRESSO";
                label = "Em progresso";
                sugestao = "Houve evolução de " + formatarPercentual(variacao) + ". Se a técnica e as repetições-alvo estiverem consistentes, considere uma pequena progressão.";
            } else if (variacao <= -2D) {
                status = "QUEDA";
                label = "Queda de carga";
                sugestao = "A carga caiu " + formatarPercentual(Math.abs(variacao)) + ". Priorize técnica e recuperação antes de aumentar a carga.";
            } else {
                status = "ESTAVEL";
                label = "Carga estável";
                sugestao = "A carga está praticamente estável. Verifique se a meta de repetições foi atingida antes de progredir.";
            }
        }

        return new ProgressaoTreinoView.ItemProgressaoView(
                atual.getRegistro().getAluno().getId(),
                atual.getRegistro().getAluno().getNome(),
                atual.getTreinoExercicio().getExercicio().getNome(),
                cargaAnterior,
                cargaAtual,
                anterior == null ? null : anterior.getRepeticoesRealizadas(),
                atual.getRepeticoesRealizadas(),
                arredondar(variacao),
                status,
                label,
                sugestao,
                DATA.format(atual.getRegistro().getDataExecucao()));
    }

    private Double extrairNumero(String valor) {
        if (valor == null || valor.isBlank()) return null;
        Matcher matcher = NUMERO.matcher(valor.replace(',', '.'));
        Double ultimo = null;
        while (matcher.find()) {
            try {
                ultimo = Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {
                // Mantém a ausência de valor numérico comparável.
            }
        }
        return ultimo;
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? "Não informado" : valor.trim();
    }

    private int prioridade(String status) {
        return switch (status) {
            case "QUEDA" -> 0;
            case "PROGRESSO" -> 1;
            case "ESTAVEL" -> 2;
            default -> 3;
        };
    }

    private double arredondar(double valor) {
        return Math.round(valor * 10D) / 10D;
    }

    private String formatarPercentual(double valor) {
        DecimalFormat format = (DecimalFormat) DecimalFormat.getNumberInstance(new Locale("pt", "BR"));
        format.applyPattern("0.0");
        return format.format(valor) + "%";
    }
}
