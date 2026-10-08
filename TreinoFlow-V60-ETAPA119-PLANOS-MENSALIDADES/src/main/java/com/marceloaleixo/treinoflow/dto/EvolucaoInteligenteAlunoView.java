package com.marceloaleixo.treinoflow.dto;

import java.util.List;

/** Etapa 86 - painel consolidado de evolução inteligente por aluno. */
public record EvolucaoInteligenteAlunoView(
        Long alunoId,
        String alunoNome,
        String objetivo,
        String treinoReferencia,
        double scoreEvolucao,
        String nivelGeral,
        String resumoGeral,
        int exerciciosAnalisados,
        int progressao,
        int estaveis,
        int atencao,
        int fadiga,
        int semDados,
        List<ExercicioEvolucaoView> exercicios) {

    public String scoreFormatado() {
        return String.format("%.0f/100", scoreEvolucao);
    }

    public record ExercicioEvolucaoView(
            Long treinoExercicioId,
            String nome,
            String grupoMuscular,
            String nivel,
            String confianca,
            String titulo,
            String resumo,
            String acao,
            String ultimaExecucao,
            Double variacaoVolume,
            Double rpeMedio,
            Double scoreTendencia,
            PrescricaoAssistidaView prescricaoAssistida) {

        public String variacaoVolumeFormatada() {
            return variacaoVolume == null ? "—" : String.format("%+.1f%%", variacaoVolume);
        }

        public String scoreFormatado() {
            return scoreTendencia == null ? "—" : String.format("%.0f/100", scoreTendencia);
        }
    }
}
