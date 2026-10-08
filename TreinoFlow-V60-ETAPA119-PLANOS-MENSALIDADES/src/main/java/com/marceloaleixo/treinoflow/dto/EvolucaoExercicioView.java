package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record EvolucaoExercicioView(String nome, String ultimaCarga, Integer ultimasRepeticoes,
                                    long sessoes, List<String> historico) {
}
