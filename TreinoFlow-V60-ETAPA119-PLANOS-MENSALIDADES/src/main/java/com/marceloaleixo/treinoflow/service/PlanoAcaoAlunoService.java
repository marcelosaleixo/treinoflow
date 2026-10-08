package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.PlanoAcaoAlunoView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.RegistroTreinoAluno;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.RegistroTreinoAlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlanoAcaoAlunoService {
    private final AlunoRepository alunos;
    private final RegistroTreinoAlunoRepository registros;

    public PlanoAcaoAlunoService(AlunoRepository alunos, RegistroTreinoAlunoRepository registros) {
        this.alunos = alunos;
        this.registros = registros;
    }

    @Transactional(readOnly = true)
    public List<PlanoAcaoAlunoView> listar(Long personalId) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio7 = hoje.minusDays(6);
        LocalDate inicio30 = hoje.minusDays(29);

        List<Aluno> ativos = alunos.findByPersonalIdAndStatusOrderByNomeAsc(personalId, "ATIVO");
        List<RegistroTreinoAluno> historico = registros.findAll().stream()
                .filter(r -> r.getAluno() != null
                        && r.getAluno().getPersonal() != null
                        && personalId.equals(r.getAluno().getPersonal().getId()))
                .filter(RegistroTreinoAluno::isConcluido)
                .toList();

        Map<Long, List<RegistroTreinoAluno>> porAluno = historico.stream()
                .collect(Collectors.groupingBy(r -> r.getAluno().getId(), LinkedHashMap::new, Collectors.toList()));

        return ativos.stream()
                .map(aluno -> montar(aluno, porAluno.getOrDefault(aluno.getId(), List.of()), inicio7, inicio30))
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparingInt(this::pesoPrioridade)
                        .thenComparing(PlanoAcaoAlunoView::ultimoTreino,
                                Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(PlanoAcaoAlunoView::nome))
                .toList();
    }

    private PlanoAcaoAlunoView montar(Aluno aluno, List<RegistroTreinoAluno> registrosAluno,
                                      LocalDate inicio7, LocalDate inicio30) {
        long sete = registrosAluno.stream().filter(r -> !r.getDataExecucao().isBefore(inicio7)).count();
        long trinta = registrosAluno.stream().filter(r -> !r.getDataExecucao().isBefore(inicio30)).count();
        LocalDate ultimo = registrosAluno.stream().map(RegistroTreinoAluno::getDataExecucao)
                .max(LocalDate::compareTo).orElse(null);
        double media = registrosAluno.stream().filter(r -> r.getNota() != null)
                .mapToInt(RegistroTreinoAluno::getNota).average().orElse(0D);

        if (ultimo == null || ultimo.isBefore(inicio30)) {
            return new PlanoAcaoAlunoView(aluno.getId(), aluno.getNome(), aluno.getTelefone(),
                    "ALTA", "RETENCAO",
                    "Não há treino concluído nos últimos 30 dias.",
                    "Faça um contato de recuperação e descubra o principal motivo da ausência.",
                    ultimo, trinta, sete, media);
        }
        if (sete == 0) {
            return new PlanoAcaoAlunoView(aluno.getId(), aluno.getNome(), aluno.getTelefone(),
                    "ALTA", "FREQUENCIA",
                    "O aluno treinou nos últimos 30 dias, mas não treinou nesta semana.",
                    "Envie uma mensagem de acompanhamento e confirme se existe alguma barreira para treinar.",
                    ultimo, trinta, sete, media);
        }
        if (trinta < 5) {
            return new PlanoAcaoAlunoView(aluno.getId(), aluno.getNome(), aluno.getTelefone(),
                    "MEDIA", "ADESAO",
                    "Foram registradas menos de 5 sessões nos últimos 30 dias.",
                    "Revise a rotina, frequência planejada e disponibilidade do aluno.",
                    ultimo, trinta, sete, media);
        }
        if (media > 0 && media < 3.5) {
            return new PlanoAcaoAlunoView(aluno.getId(), aluno.getNome(), aluno.getTelefone(),
                    "MEDIA", "SATISFACAO",
                    "A média das avaliações está abaixo de 3,5.",
                    "Abra o histórico, leia os feedbacks e avalie ajustes no treino.",
                    ultimo, trinta, sete, media);
        }
        return null;
    }

    private int pesoPrioridade(PlanoAcaoAlunoView view) {
        return switch (view.prioridade()) {
            case "ALTA" -> 0;
            case "MEDIA" -> 1;
            default -> 2;
        };
    }
}
