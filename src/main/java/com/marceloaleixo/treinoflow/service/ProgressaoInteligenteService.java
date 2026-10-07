package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.RecomendacaoProgressaoView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.ExecucaoSerie;
import com.marceloaleixo.treinoflow.entity.TreinoExercicio;
import com.marceloaleixo.treinoflow.repository.ExecucaoSerieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Motor determinístico da Etapa 83. Não altera a prescrição sozinho: apenas
 * transforma histórico real de séries em uma sugestão explicável para o aluno.
 */
@Service
public class ProgressaoInteligenteService {
    private static final Pattern NUMERO = Pattern.compile("(\\d+(?:[.,]\\d+)?)");
    private static final Pattern FAIXA = Pattern.compile("(\\d+)\\s*[-–aA]\\s*(\\d+)");

    private final ExecucaoSerieRepository series;

    public ProgressaoInteligenteService(ExecucaoSerieRepository series) {
        this.series = series;
    }

    @Transactional(readOnly = true)
    public RecomendacaoProgressaoView recomendar(TreinoExercicio item, Aluno aluno) {
        List<ExecucaoSerie> historico = series.buscarHistoricoAnterior(item.getId(), aluno.getId(), LocalDate.now());
        if (historico.isEmpty()) {
            return new RecomendacaoProgressaoView(
                    "INICIAL", "Primeira referência", 
                    "Ainda não há séries anteriores deste exercício registradas.",
                    "Comece pela carga prescrita pelo Personal e registre a execução.", null, null);
        }

        ExecucaoSerie ultima = historico.get(0);
        String ultimaExecucao = formatar(ultima);
        Integer rpe = ultima.getRpe();
        int[] faixa = faixaRepeticoes(item.getRepeticoes());
        Integer reps = ultima.getRepeticoesRealizadas();

        if (rpe != null && rpe >= 9) {
            return new RecomendacaoProgressaoView(
                    "ATENCAO", "Priorize a execução", 
                    "A última série registrada teve RPE " + rpe + ", indicando esforço muito alto.",
                    "Mantenha a carga e tente atingir a faixa prescrita com técnica consistente antes de aumentar.",
                    ultimaExecucao, rpe);
        }

        if (reps != null && faixa[1] > 0 && reps >= faixa[1] && (rpe == null || rpe <= 8)) {
            return new RecomendacaoProgressaoView(
                    "PROGREDIR", "Há sinal de progressão", 
                    "Você atingiu o topo da faixa de repetições na última execução" + (rpe == null ? "." : " com RPE " + rpe + "."),
                    "Se a técnica estiver boa, converse com o Personal sobre um pequeno aumento de carga.",
                    ultimaExecucao, rpe);
        }

        if (reps != null && faixa[0] > 0 && reps < faixa[0]) {
            return new RecomendacaoProgressaoView(
                    "CONSOLIDAR", "Consolide a carga", 
                    "A última execução ficou abaixo do início da faixa prescrita.",
                    "Mantenha a carga atual e priorize alcançar a faixa de repetições com boa técnica.",
                    ultimaExecucao, rpe);
        }

        return new RecomendacaoProgressaoView(
                "MANTER", "Mantenha por enquanto", 
                "O histórico ainda não mostra evidência suficiente para recomendar aumento de carga.",
                "Repita a carga atual e registre as séries para melhorar a próxima recomendação.",
                ultimaExecucao, rpe);
    }

    private String formatar(ExecucaoSerie serie) {
        String carga = serie.getCargaRealizada() == null || serie.getCargaRealizada().isBlank()
                ? "carga não informada" : serie.getCargaRealizada();
        String reps = serie.getRepeticoesRealizadas() == null ? "reps não informadas" : serie.getRepeticoesRealizadas() + " reps";
        return carga + " × " + reps;
    }

    private int[] faixaRepeticoes(String valor) {
        if (valor == null || valor.isBlank()) return new int[]{0, 0};
        String normalizado = valor.trim().toLowerCase(Locale.ROOT);
        Matcher faixa = FAIXA.matcher(normalizado);
        if (faixa.find()) {
            return new int[]{Integer.parseInt(faixa.group(1)), Integer.parseInt(faixa.group(2))};
        }
        Matcher numero = NUMERO.matcher(normalizado);
        if (numero.find()) {
            int n = (int) Double.parseDouble(numero.group(1).replace(',', '.'));
            return new int[]{n, n};
        }
        return new int[]{0, 0};
    }
}
