package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.HistoricoPerformanceDashboardView;
import com.marceloaleixo.treinoflow.dto.PerformanceMensalView;
import com.marceloaleixo.treinoflow.entity.MetaComercial;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.repository.MetaComercialRepository;
import com.marceloaleixo.treinoflow.repository.RegistroTreinoAlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class HistoricoPerformanceService {
    private final MetaComercialRepository metas;
    private final RegistroTreinoAlunoRepository registros;
    private final InteracaoCrmRepository interacoes;

    public HistoricoPerformanceService(MetaComercialRepository metas,
                                        RegistroTreinoAlunoRepository registros,
                                        InteracaoCrmRepository interacoes) {
        this.metas = metas;
        this.registros = registros;
        this.interacoes = interacoes;
    }

    @Transactional(readOnly = true)
    public HistoricoPerformanceDashboardView dashboard(Long personalId) {
        LocalDate primeiroMes = LocalDate.now().withDayOfMonth(1).minusMonths(5);
        List<PerformanceMensalView> meses = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            meses.add(calcularMes(personalId, primeiroMes.plusMonths(i)));
        }

        PerformanceMensalView atual = meses.get(meses.size() - 1);
        PerformanceMensalView anterior = meses.size() > 1 ? meses.get(meses.size() - 2) : atual;
        PerformanceMensalView melhor = meses.stream()
                .max(Comparator.comparingInt(PerformanceMensalView::pontos)
                        .thenComparingInt(PerformanceMensalView::progressoGeral))
                .orElse(atual);

        return new HistoricoPerformanceDashboardView(
                meses.get(0).mesLabel() + " a " + atual.mesLabel(),
                atual,
                melhor,
                anterior,
                atual.pontos() - anterior.pontos(),
                meses
        );
    }

    private PerformanceMensalView calcularMes(Long personalId, LocalDate mes) {
        LocalDate fim = mes.plusMonths(1);
        MetaComercial meta = metas.findByPersonalIdAndMesReferencia(personalId, mes)
                .orElseGet(() -> metaPadrao());

        long treinos = registros.countTreinosConcluidosNoPeriodo(personalId, mes, fim);
        long alunosAcompanhados = registros.countAlunosComTreinoNoPeriodo(personalId, mes, fim);
        long recuperacoes = interacoes.buscarRetencoesDesde(personalId, mes.atStartOfDay()).stream()
                .filter(i -> i.getDataContato() != null && i.getDataContato().isBefore(fim.atStartOfDay()))
                .filter(i -> i.getResultado() == ResultadoCrm.RECUPERADO || i.getResultado() == ResultadoCrm.RENOVADO)
                .map(i -> i.getAluno().getId())
                .distinct()
                .count();

        int percentualAlunos = progresso(alunosAcompanhados, meta.getMetaAlunosAtivos());
        int percentualTreinos = progresso(treinos, meta.getMetaTreinos());
        int percentualRecuperacoes = progresso(recuperacoes, meta.getMetaRecuperacoes());
        int progressoGeral = Math.round((percentualAlunos + percentualTreinos + percentualRecuperacoes) / 3f);

        int pontosMetas = pontosPorIndicador(percentualAlunos, percentualTreinos, percentualRecuperacoes);
        int pontosTreinos = (int) Math.min(100, treinos / 5);
        int pontosRecuperacoes = (int) Math.min(100, recuperacoes * 10);
        int pontosAcompanhamento = (int) Math.min(100, alunosAcompanhados * 2);

        int concluidos = 0;
        int bonus = 0;
        if (recuperacoes >= meta.getMetaRecuperacoes()) {
            concluidos++;
            bonus += 50;
        }
        if (treinos >= meta.getMetaTreinos()) {
            concluidos++;
            bonus += 40;
        }
        if (alunosAcompanhados >= meta.getMetaAlunosAtivos()) {
            concluidos++;
            bonus += 30;
        }
        if (progressoGeral >= 100) {
            concluidos++;
            bonus += 100;
        }

        int pontos = pontosMetas + pontosTreinos + pontosRecuperacoes + pontosAcompanhamento + bonus;
        return new PerformanceMensalView(label(mes), pontos, nivel(pontos), progressoGeral,
                pontosMetas, pontosTreinos, pontosRecuperacoes, pontosAcompanhamento,
                alunosAcompanhados, treinos, recuperacoes, bonus, concluidos);
    }

    private MetaComercial metaPadrao() {
        return new MetaComercial();
    }

    private int progresso(long atual, int meta) {
        return meta <= 0 ? 100 : Math.min(100, (int) Math.round(atual * 100D / meta));
    }

    private int pontosPorIndicador(int alunos, int treinos, int recuperacoes) {
        int pontos = 0;
        pontos += faixa(alunos);
        pontos += faixa(treinos);
        pontos += faixa(recuperacoes);
        return pontos;
    }

    private int faixa(int percentual) {
        return percentual >= 100 ? 50 : percentual >= 75 ? 30 : percentual >= 50 ? 15 : 0;
    }

    private String nivel(int pontos) {
        if (pontos >= 500) return "Elite";
        if (pontos >= 250) return "Alta performance";
        if (pontos >= 100) return "Em evolução";
        return "Iniciante";
    }

    private String label(LocalDate mes) {
        String nome = mes.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        return Character.toUpperCase(nome.charAt(0)) + nome.substring(1) + "/" + mes.getYear();
    }
}
