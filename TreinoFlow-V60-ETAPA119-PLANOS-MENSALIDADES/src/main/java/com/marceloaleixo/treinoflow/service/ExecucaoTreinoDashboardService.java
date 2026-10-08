package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.ExecucaoTreinoDashboardView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.ExecucaoExercicio;
import com.marceloaleixo.treinoflow.entity.RegistroTreinoAluno;
import com.marceloaleixo.treinoflow.entity.TreinoExercicio;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.ExecucaoExercicioRepository;
import com.marceloaleixo.treinoflow.repository.RegistroTreinoAlunoRepository;
import com.marceloaleixo.treinoflow.repository.TreinoExercicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExecucaoTreinoDashboardService {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AlunoRepository alunos;
    private final RegistroTreinoAlunoRepository registros;
    private final TreinoExercicioRepository treinoExercicios;
    private final ExecucaoExercicioRepository execucoes;

    public ExecucaoTreinoDashboardService(AlunoRepository alunos,
                                           RegistroTreinoAlunoRepository registros,
                                           TreinoExercicioRepository treinoExercicios,
                                           ExecucaoExercicioRepository execucoes) {
        this.alunos = alunos;
        this.registros = registros;
        this.treinoExercicios = treinoExercicios;
        this.execucoes = execucoes;
    }

    @Transactional(readOnly = true)
    public ExecucaoTreinoDashboardView montar(Long personalId) {
        LocalDate inicio = LocalDate.now().minusDays(29);

        List<Aluno> alunosAtivos = alunos.findByPersonalIdAndStatusOrderByNomeAsc(personalId, "ATIVO");
        List<RegistroTreinoAluno> sessoes = registros.buscarConcluidosDoPersonalDesde(personalId, inicio);
        List<ExecucaoExercicio> execucoesPeriodo = execucoes.buscarDoPersonalDesde(personalId, inicio);
        List<TreinoExercicio> prescricoes = treinoExercicios.listarDoPersonal(personalId);

        Map<Long, Long> prescritosPorTreino = new HashMap<>();
        Map<Long, List<TreinoExercicio>> itensPorTreino = new HashMap<>();
        Map<Long, String> nomeExercicioPorId = new HashMap<>();
        for (TreinoExercicio item : prescricoes) {
            prescritosPorTreino.merge(item.getTreino().getId(), 1L, Long::sum);
            itensPorTreino.computeIfAbsent(item.getTreino().getId(), k -> new ArrayList<>()).add(item);
            nomeExercicioPorId.put(item.getExercicio().getId(), item.getExercicio().getNome());
        }

        Map<Long, List<ExecucaoExercicio>> execucoesPorRegistro = new HashMap<>();
        for (ExecucaoExercicio execucao : execucoesPeriodo) {
            execucoesPorRegistro.computeIfAbsent(execucao.getRegistro().getId(), k -> new ArrayList<>()).add(execucao);
        }

        long totalPrescritos = 0;
        long totalConcluidos = 0;
        long observacoes = 0;
        double somaNotas = 0;
        long quantidadeNotas = 0;

        Map<Long, AlunoAccumulator> porAluno = new LinkedHashMap<>();
        Map<Long, ExercicioAccumulator> porExercicio = new LinkedHashMap<>();

        for (RegistroTreinoAluno registro : sessoes) {
            long prescritos = prescritosPorTreino.getOrDefault(registro.getTreino().getId(), 0L);
            List<ExecucaoExercicio> realizadas = execucoesPorRegistro.getOrDefault(registro.getId(), List.of());
            long concluidos = realizadas.stream().filter(ExecucaoExercicio::isConcluido).count();

            totalPrescritos += prescritos;
            totalConcluidos += Math.min(concluidos, prescritos == 0 ? concluidos : prescritos);
            if (registro.getNota() != null) {
                somaNotas += registro.getNota();
                quantidadeNotas++;
            }

            AlunoAccumulator aluno = porAluno.computeIfAbsent(registro.getAluno().getId(),
                    k -> new AlunoAccumulator(registro.getAluno().getNome()));
            aluno.sessoes++;
            aluno.prescritos += prescritos;
            aluno.concluidos += Math.min(concluidos, prescritos == 0 ? concluidos : prescritos);
            if (aluno.ultimoTreino == null || registro.getDataExecucao().isAfter(aluno.ultimoTreino)) {
                aluno.ultimoTreino = registro.getDataExecucao();
            }
            if (registro.getNota() != null) {
                aluno.somaNotas += registro.getNota();
                aluno.quantidadeNotas++;
            }

            for (ExecucaoExercicio execucao : realizadas) {
                if (!execucao.isConcluido()) continue;
                if (execucao.getObservacao() != null && !execucao.getObservacao().isBlank()) observacoes++;

                Long exercicioId = execucao.getTreinoExercicio().getExercicio().getId();
                ExercicioAccumulator exercicio = porExercicio.computeIfAbsent(exercicioId,
                        k -> new ExercicioAccumulator(nomeExercicioPorId.getOrDefault(exercicioId, "Exercício")));
                exercicio.sessoesConcluidas++;
                if (execucao.getCargaRealizada() != null && !execucao.getCargaRealizada().isBlank() && exercicio.ultimaCarga == null) {
                    exercicio.ultimaCarga = execucao.getCargaRealizada();
                }
                if (execucao.getRepeticoesRealizadas() != null && exercicio.ultimasRepeticoes == null) {
                    exercicio.ultimasRepeticoes = execucao.getRepeticoesRealizadas();
                }
                if (execucao.getObservacao() != null && !execucao.getObservacao().isBlank()) exercicio.observacoes++;
            }
        }

        // Exercícios prescritos entram no denominador mesmo quando não houve checklist salvo.
        // Cada item da ficha conta como uma ocorrência, inclusive se o mesmo exercício aparecer duas vezes.
        for (RegistroTreinoAluno registro : sessoes) {
            for (TreinoExercicio item : itensPorTreino.getOrDefault(registro.getTreino().getId(), List.of())) {
                Long exercicioId = item.getExercicio().getId();
                ExercicioAccumulator exercicio = porExercicio.computeIfAbsent(exercicioId,
                        k -> new ExercicioAccumulator(nomeExercicioPorId.getOrDefault(exercicioId, "Exercício")));
                exercicio.sessoesPrescritas++;
            }
        }

        List<ExecucaoTreinoDashboardView.AlunoExecucaoView> alunosView = alunosAtivos.stream()
                .map(aluno -> {
                    AlunoAccumulator acc = porAluno.getOrDefault(aluno.getId(), new AlunoAccumulator(aluno.getNome()));
                    return new ExecucaoTreinoDashboardView.AlunoExecucaoView(
                            aluno.getId(), aluno.getNome(), acc.sessoes, acc.prescritos, acc.concluidos,
                            percentual(acc.concluidos, acc.prescritos),
                            acc.ultimoTreino == null ? "Nenhum" : DATA.format(acc.ultimoTreino),
                            acc.quantidadeNotas == 0 ? 0D : acc.somaNotas / acc.quantidadeNotas);
                })
                .sorted(Comparator.comparingDouble(ExecucaoTreinoDashboardView.AlunoExecucaoView::taxaConclusao).reversed()
                        .thenComparing(ExecucaoTreinoDashboardView.AlunoExecucaoView::nome))
                .toList();

        List<ExecucaoTreinoDashboardView.ExercicioExecucaoView> exerciciosView = porExercicio.values().stream()
                .map(acc -> new ExecucaoTreinoDashboardView.ExercicioExecucaoView(
                        acc.nome, acc.sessoesPrescritas, acc.sessoesConcluidas,
                        percentual(acc.sessoesConcluidas, acc.sessoesPrescritas),
                        acc.ultimaCarga, acc.ultimasRepeticoes, acc.observacoes))
                .sorted(Comparator.comparingDouble(ExecucaoTreinoDashboardView.ExercicioExecucaoView::taxaConclusao).reversed()
                        .thenComparing(ExecucaoTreinoDashboardView.ExercicioExecucaoView::nome))
                .limit(20)
                .toList();

        return new ExecucaoTreinoDashboardView(
                sessoes.size(), totalPrescritos, totalConcluidos,
                percentual(totalConcluidos, totalPrescritos),
                quantidadeNotas == 0 ? 0D : somaNotas / quantidadeNotas,
                observacoes, alunosView, exerciciosView);
    }

    private double percentual(long realizado, long prescrito) {
        if (prescrito <= 0) return 0D;
        return Math.min(100D, (realizado * 100D) / prescrito);
    }

    private static class AlunoAccumulator {
        private final String nome;
        private long sessoes;
        private long prescritos;
        private long concluidos;
        private LocalDate ultimoTreino;
        private double somaNotas;
        private long quantidadeNotas;
        private AlunoAccumulator(String nome) { this.nome = nome; }
    }

    private static class ExercicioAccumulator {
        private final String nome;
        private long sessoesPrescritas;
        private long sessoesConcluidas;
        private String ultimaCarga;
        private Integer ultimasRepeticoes;
        private long observacoes;
        private ExercicioAccumulator(String nome) { this.nome = nome; }
    }
}
