package com.marceloaleixo.treinoflow.dto;

import com.marceloaleixo.treinoflow.enums.ResultadoCrm;

import java.time.LocalDateTime;

public record RetencaoResultadoView(
        Long alunoId,
        String nome,
        int score,
        String nivel,
        String nivelCss,
        ResultadoCrm resultado,
        String resultadoDescricao,
        LocalDateTime dataContato,
        String assunto
) {}
