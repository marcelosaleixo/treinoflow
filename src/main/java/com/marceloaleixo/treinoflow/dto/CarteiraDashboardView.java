package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record CarteiraDashboardView(
        long total,
        long saudaveis,
        long atencao,
        long risco,
        long criticos,
        long recuperados,
        double riscoMedio,
        List<CarteiraAlunoView> alunos
) {}
