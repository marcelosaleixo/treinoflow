package com.marceloaleixo.treinoflow.dto;

public record RecomendacaoPerformanceView(
        String tipo,
        String titulo,
        String descricao,
        String acao,
        String prioridade
) {
}
