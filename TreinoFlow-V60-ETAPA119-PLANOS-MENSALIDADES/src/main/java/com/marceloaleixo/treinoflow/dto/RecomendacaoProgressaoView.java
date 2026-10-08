package com.marceloaleixo.treinoflow.dto;

/** Recomendação simples e explicável para progressão do exercício. */
public record RecomendacaoProgressaoView(
        String nivel,
        String titulo,
        String mensagem,
        String acao,
        String ultimaExecucao,
        Integer rpe
) {
    public boolean temHistorico() {
        return ultimaExecucao != null && !ultimaExecucao.isBlank();
    }
}
