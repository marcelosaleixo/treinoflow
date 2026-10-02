package com.marceloaleixo.treinoflow.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TreinoAlunoView(
        String nome,
        String descricao,
        String nomeAluno,
        String nomePersonal,
        LocalDateTime dataLiberacao,
        LocalDateTime acessoExpiraEm,
        List<ExercicioView> exercicios) {
    public record ExercicioView(
            int ordem,
            String nome,
            String grupoMuscular,
            String descricao,
            String urlVideo,
            Integer series,
            String repeticoes,
            String carga,
            Integer descansoSegundos,
            String observacao) {}
}
