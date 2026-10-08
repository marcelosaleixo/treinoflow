package com.marceloaleixo.treinoflow.dto;

import java.time.LocalDate;
import java.util.List;

public record PerfilInteligenteAlunoView(
        String faixaEtaria,
        Integer idade,
        String objetivo,
        String status,
        String tempoRelacionamento,
        long treinosCadastrados,
        long treinosLiberados,
        long sessoesConcluidas,
        long sessoesUltimos30Dias,
        LocalDate ultimaSessao,
        String nivelEngajamento,
        String perfil,
        String resumo,
        List<String> sinais,
        int aderenciaScore,
        String aderenciaNivel,
        int frequenciaScore,
        int recenciaScore,
        int continuidadeScore,
        int execucaoScore,
        int riscoAbandonoScore,
        String riscoAbandonoNivel,
        String riscoAbandonoAcao,
        java.util.List<String> riscoAbandonoSinais,
        int radarAderencia,
        int radarEvolucao,
        int radarEngajamento,
        int radarRisco,
        int radarScore,
        String radarNivel,
        String radarAcao
) {}
