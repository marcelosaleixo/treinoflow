package com.marceloaleixo.treinoflow.dto;

import com.marceloaleixo.treinoflow.enums.CanalCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;

import java.time.LocalDate;

/** Etapa 124: objeto de comando para registrar uma ação executada pelo Radar. */
public record RadarAcaoRegistro(
        Long personalId,
        Long alunoId,
        String tipo,
        String assunto,
        String motivo,
        String acao,
        CanalCrm canal,
        ResultadoCrm resultado,
        LocalDate proximaAcao,
        String observacao,
        int scoreRiscoAntes
) {}
