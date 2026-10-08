package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record EvolucaoAlunoView(long sessoesConcluidas, long sessoesUltimos7Dias,
                                long sessoesUltimos30Dias, double mediaNotas,
                                List<EvolucaoExercicioView> exercicios) {
}
