package com.marceloaleixo.treinoflow.dto;

import com.marceloaleixo.treinoflow.entity.Agendamento;
import java.util.List;

public record PresencaDashboardView(
        long totalAgendamentos,
        long realizados,
        long faltas,
        long confirmados,
        long pendentes,
        double taxaComparecimento,
        long alunosComFalta,
        long alunosEmRisco,
        List<PresencaAlunoView> alunos,
        List<Agendamento> ultimosAgendamentos
) {}
