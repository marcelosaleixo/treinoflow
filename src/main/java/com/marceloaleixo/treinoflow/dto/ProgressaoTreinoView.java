package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record ProgressaoTreinoView(
        long avaliados,
        long emProgressao,
        long estaveis,
        long emQueda,
        long semComparacao,
        List<ItemProgressaoView> itens) {

    public record ItemProgressaoView(
            Long alunoId,
            String alunoNome,
            String exercicioNome,
            String cargaAnterior,
            String cargaAtual,
            Integer repeticoesAnteriores,
            Integer repeticoesAtuais,
            double variacaoPercentual,
            String status,
            String statusLabel,
            String sugestao,
            String dataAtual) {
    }
}
