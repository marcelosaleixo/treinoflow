package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record ExecucaoTreinoDashboardView(
        long sessoes30Dias,
        long exerciciosPrescritos,
        long exerciciosConcluidos,
        double taxaConclusao,
        double mediaNotas,
        long observacoesRegistradas,
        List<AlunoExecucaoView> alunos,
        List<ExercicioExecucaoView> exercicios) {

    public record AlunoExecucaoView(
            Long alunoId,
            String nome,
            long sessoes,
            long prescritos,
            long concluidos,
            double taxaConclusao,
            String ultimoTreino,
            double mediaNota) {
    }

    public record ExercicioExecucaoView(
            String nome,
            long sessoesPrescritas,
            long sessoesConcluidas,
            double taxaConclusao,
            String ultimaCarga,
            Integer ultimasRepeticoes,
            long observacoes) {
    }
}
