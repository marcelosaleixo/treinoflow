package com.marceloaleixo.treinoflow.dto;

import java.time.LocalDate;

public record AlunoPerformanceView(
        Long alunoId,
        String nome,
        long sessoes30Dias,
        long sessoes7Dias,
        LocalDate ultimoTreino,
        double mediaNota,
        String status
) {
}
