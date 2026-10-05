package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.AlunoPerformanceView;
import com.marceloaleixo.treinoflow.dto.DashboardPerformanceView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.RegistroTreinoAluno;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.RegistroTreinoAlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardPerformanceService {
    private final AlunoRepository alunos;
    private final RegistroTreinoAlunoRepository registros;
    private final InteligenciaTreinoService inteligenciaTreino;

    public DashboardPerformanceService(AlunoRepository alunos, RegistroTreinoAlunoRepository registros, InteligenciaTreinoService inteligenciaTreino) {
        this.alunos = alunos;
        this.registros = registros;
        this.inteligenciaTreino = inteligenciaTreino;
    }

    @Transactional(readOnly = true)
    public DashboardPerformanceView montar(Long personalId) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio7 = hoje.minusDays(6);
        LocalDate inicio30 = hoje.minusDays(29);
        List<Aluno> alunosAtivos = alunos.findByPersonalIdAndStatusOrderByNomeAsc(personalId, "ATIVO");
        // Compatibilidade: utiliza o contrato base do JpaRepository para evitar
        // erro de classe compilada antiga quando o método específico do repository
        // ainda não estiver refletido no ambiente em execução.
        List<RegistroTreinoAluno> historico = registros.findAll().stream()
                .filter(r -> r.getAluno() != null
                        && r.getAluno().getPersonal() != null
                        && personalId.equals(r.getAluno().getPersonal().getId()))
                .toList();

        List<RegistroTreinoAluno> concluidos = historico.stream()
                .filter(RegistroTreinoAluno::isConcluido)
                .toList();

        long sessoes7 = concluidos.stream().filter(r -> !r.getDataExecucao().isBefore(inicio7)).count();
        long sessoes30 = concluidos.stream().filter(r -> !r.getDataExecucao().isBefore(inicio30)).count();
        double media = concluidos.stream().filter(r -> r.getNota() != null)
                .mapToInt(RegistroTreinoAluno::getNota).average().orElse(0D);

        Map<Long, List<RegistroTreinoAluno>> porAluno = concluidos.stream()
                .collect(Collectors.groupingBy(r -> r.getAluno().getId(), LinkedHashMap::new, Collectors.toList()));

        List<AlunoPerformanceView> desempenho = alunosAtivos.stream().map(aluno -> {
            List<RegistroTreinoAluno> registrosAluno = porAluno.getOrDefault(aluno.getId(), List.of());
            long sete = registrosAluno.stream().filter(r -> !r.getDataExecucao().isBefore(inicio7)).count();
            long trinta = registrosAluno.stream().filter(r -> !r.getDataExecucao().isBefore(inicio30)).count();
            LocalDate ultimo = registrosAluno.stream().map(RegistroTreinoAluno::getDataExecucao).max(LocalDate::compareTo).orElse(null);
            double mediaAluno = registrosAluno.stream().filter(r -> r.getNota() != null)
                    .mapToInt(RegistroTreinoAluno::getNota).average().orElse(0D);
            String status = ultimo == null || ultimo.isBefore(inicio30) ? "ATENÇÃO" : (sete > 0 ? "ATIVO" : "SEMANA FRACA");
            return new AlunoPerformanceView(aluno.getId(), aluno.getNome(), trinta, sete, ultimo, mediaAluno, status);
        }).toList();

        List<AlunoPerformanceView> risco = desempenho.stream()
                .filter(a -> "ATENÇÃO".equals(a.status()) || a.sessoes7Dias() == 0)
                .sorted(Comparator.comparing(AlunoPerformanceView::sessoes30Dias).thenComparing(AlunoPerformanceView::nome))
                .limit(8).toList();
        List<AlunoPerformanceView> ativos = desempenho.stream()
                .filter(a -> a.sessoes30Dias() > 0)
                .sorted(Comparator.comparing(AlunoPerformanceView::sessoes30Dias).reversed().thenComparing(AlunoPerformanceView::nome))
                .limit(8).toList();

        List<DashboardPerformanceView.SemanaPerformanceView> semanas = new ArrayList<>();
        LocalDate inicioSemanaAtual = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (int i = 7; i >= 0; i--) {
            LocalDate inicio = inicioSemanaAtual.minusWeeks(i);
            LocalDate fim = inicio.plusDays(6);
            long quantidade = concluidos.stream()
                    .filter(r -> !r.getDataExecucao().isBefore(inicio) && !r.getDataExecucao().isAfter(fim))
                    .count();
            semanas.add(new DashboardPerformanceView.SemanaPerformanceView(
                    inicio.getDayOfMonth() + "/" + inicio.getMonthValue(), quantidade));
        }

        long comTreino30 = desempenho.stream().filter(a -> a.sessoes30Dias() > 0).count();
        long semTreino30 = Math.max(0, alunosAtivos.size() - comTreino30);
        var recomendacoes = inteligenciaTreino.gerar(desempenho, sessoes7, sessoes30, media, semTreino30);
        return new DashboardPerformanceView(sessoes30, sessoes7, media, comTreino30, semTreino30, semanas, risco, ativos, recomendacoes);
    }
}
