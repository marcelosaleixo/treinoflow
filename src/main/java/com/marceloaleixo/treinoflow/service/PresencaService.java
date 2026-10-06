package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.PresencaAlunoView;
import com.marceloaleixo.treinoflow.dto.PresencaDashboardView;
import com.marceloaleixo.treinoflow.entity.Agendamento;
import com.marceloaleixo.treinoflow.repository.AgendamentoRepository;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PresencaService {
    private final AgendamentoRepository agendamentos;
    private final AlunoRepository alunos;

    public PresencaService(AgendamentoRepository agendamentos, AlunoRepository alunos) {
        this.agendamentos = agendamentos;
        this.alunos = alunos;
    }

    public PresencaDashboardView montar(Long personalId) {
        if (personalId == null || personalId <= 0) {
            throw new IllegalArgumentException("Personal inválido.");
        }

        LocalDate hoje = LocalDate.now();
        LocalDate inicioData = hoje.minusDays(29);
        LocalDateTime inicio = inicioData.atStartOfDay();
        LocalDateTime fim = hoje.plusDays(1).atStartOfDay();

        List<Agendamento> periodo = agendamentos.buscarDoPeriodoComAluno(personalId, inicio, fim);
        List<Agendamento> considerados = periodo.stream()
                .filter(a -> !"CANCELADO".equals(a.getStatus()))
                .toList();

        long realizados = contar(considerados, "REALIZADO");
        long faltas = contar(considerados, "FALTOU");
        long confirmados = contar(considerados, "CONFIRMADO");
        long pendentes = considerados.stream()
                .filter(a -> "AGENDADO".equals(a.getStatus()) || "CONFIRMADO".equals(a.getStatus()))
                .filter(a -> a.getInicio() != null && a.getInicio().isBefore(LocalDateTime.now()))
                .count();
        long baseComparecimento = realizados + faltas;
        double taxa = baseComparecimento == 0 ? 0D : (realizados * 100D) / baseComparecimento;

        Map<Long, List<Agendamento>> porAluno = considerados.stream()
                .filter(a -> a.getAluno() != null && a.getAluno().getId() != null)
                .collect(Collectors.groupingBy(a -> a.getAluno().getId(), LinkedHashMap::new, Collectors.toList()));

        List<PresencaAlunoView> linhas = alunos.findByPersonalIdAndStatusOrderByNomeAsc(personalId, "ATIVO")
                .stream()
                .map(aluno -> montarAluno(aluno.getId(), aluno.getNome(), porAluno.getOrDefault(aluno.getId(), List.of())))
                .sorted(Comparator.comparingInt(this::pesoSituacao).thenComparing(PresencaAlunoView::nome, String.CASE_INSENSITIVE_ORDER))
                .toList();

        long alunosComFalta = linhas.stream().filter(v -> v.faltas() > 0).count();
        long alunosEmRisco = linhas.stream().filter(v -> "RISCO".equals(v.situacao())).count();
        List<Agendamento> ultimos = periodo.stream()
                .filter(a -> a.getInicio() != null && !a.getInicio().isAfter(LocalDateTime.now()))
                .limit(12).toList();

        return new PresencaDashboardView(
                considerados.size(), realizados, faltas, confirmados, pendentes,
                taxa, alunosComFalta, alunosEmRisco, linhas, ultimos);
    }

    private PresencaAlunoView montarAluno(Long id, String nome, List<Agendamento> agenda) {
        long realizados = contar(agenda, "REALIZADO");
        long faltas = contar(agenda, "FALTOU");
        long confirmados = contar(agenda, "CONFIRMADO");
        long pendentes = agenda.stream()
                .filter(a -> ("AGENDADO".equals(a.getStatus()) || "CONFIRMADO".equals(a.getStatus()))
                        && a.getInicio() != null && a.getInicio().isBefore(LocalDateTime.now()))
                .count();
        long base = realizados + faltas;
        double taxa = base == 0 ? 0D : (realizados * 100D) / base;
        String situacao;
        if (faltas >= 2 || (base >= 3 && taxa < 75D)) {
            situacao = "RISCO";
        } else if (faltas == 1 || pendentes > 0) {
            situacao = "ATENÇÃO";
        } else if (base > 0 && taxa >= 90D) {
            situacao = "EXCELENTE";
        } else {
            situacao = "NORMAL";
        }
        return new PresencaAlunoView(id, nome, agenda.size(), realizados, faltas,
                confirmados, pendentes, taxa, situacao);
    }

    private long contar(List<Agendamento> lista, String status) {
        return lista.stream().filter(a -> Objects.equals(status, a.getStatus())).count();
    }

    private int pesoSituacao(PresencaAlunoView view) {
        return switch (view.situacao()) {
            case "RISCO" -> 0;
            case "ATENÇÃO" -> 1;
            case "NORMAL" -> 2;
            default -> 3;
        };
    }
}
