package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.EvolucaoAlunoView;
import com.marceloaleixo.treinoflow.dto.EvolucaoExercicioView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.ExecucaoExercicio;
import com.marceloaleixo.treinoflow.entity.RegistroTreinoAluno;
import com.marceloaleixo.treinoflow.repository.ExecucaoExercicioRepository;
import com.marceloaleixo.treinoflow.repository.RegistroTreinoAlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EvolucaoAlunoService {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final ExecucaoExercicioRepository execucoes;
    private final RegistroTreinoAlunoRepository registros;

    public EvolucaoAlunoService(ExecucaoExercicioRepository execucoes, RegistroTreinoAlunoRepository registros) {
        this.execucoes = execucoes;
        this.registros = registros;
    }

    @Transactional(readOnly = true)
    public EvolucaoAlunoView montar(Aluno aluno) {
        LocalDate hoje = LocalDate.now();
        List<RegistroTreinoAluno> historico = registros.buscarHistorico(aluno.getId());
        long sessoes = historico.stream().filter(RegistroTreinoAluno::isConcluido).count();
        long sete = historico.stream().filter(r -> r.isConcluido() && !r.getDataExecucao().isBefore(hoje.minusDays(6))).count();
        long trinta = historico.stream().filter(r -> r.isConcluido() && !r.getDataExecucao().isBefore(hoje.minusDays(29))).count();
        double media = historico.stream().filter(r -> r.isConcluido() && r.getNota() != null)
                .mapToInt(RegistroTreinoAluno::getNota).average().orElse(0D);

        Map<Long, EvolucaoBuilder> porExercicio = new LinkedHashMap<>();
        for (ExecucaoExercicio execucao : execucoes.buscarHistoricoDoAluno(aluno.getId())) {
            if (!execucao.isConcluido()) continue;
            Long exercicioId = execucao.getTreinoExercicio().getExercicio().getId();
            EvolucaoBuilder b = porExercicio.computeIfAbsent(exercicioId,
                    k -> new EvolucaoBuilder(execucao.getTreinoExercicio().getExercicio().getNome()));
            b.sessoes++;
            String carga = execucao.getCargaRealizada();
            Integer reps = execucao.getRepeticoesRealizadas();
            if (b.ultimaCarga == null && (carga != null && !carga.isBlank())) b.ultimaCarga = carga;
            if (b.ultimasRepeticoes == null && reps != null) b.ultimasRepeticoes = reps;
            b.historico.add(DATA.format(execucao.getRegistro().getDataExecucao()) + " · "
                    + (carga == null || carga.isBlank() ? "carga não informada" : carga)
                    + (reps == null ? "" : " · " + reps + " reps"));
        }

        List<EvolucaoExercicioView> exercicios = porExercicio.values().stream()
                .map(EvolucaoBuilder::build).toList();
        return new EvolucaoAlunoView(sessoes, sete, trinta, media, exercicios);
    }

    private static class EvolucaoBuilder {
        private final String nome;
        private String ultimaCarga;
        private Integer ultimasRepeticoes;
        private long sessoes;
        private final List<String> historico = new ArrayList<>();
        private EvolucaoBuilder(String nome) { this.nome = nome; }
        private EvolucaoExercicioView build() {
            return new EvolucaoExercicioView(nome, ultimaCarga, ultimasRepeticoes, sessoes,
                    historico.stream().limit(8).toList());
        }
    }
}
