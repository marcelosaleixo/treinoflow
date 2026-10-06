package com.marceloaleixo.treinoflow.dto;

public record PresencaAlunoView(
        Long alunoId,
        String nome,
        long agendados,
        long realizados,
        long faltas,
        long confirmados,
        long pendentes,
        double taxaComparecimento,
        String situacao
) {}
