package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record RecomendacaoAdaptativaDashboardView(
        String periodo,
        long casos,
        long recomendacoesAltaConfianca,
        long recomendacoesBaixaConfianca,
        String resumo,
        List<Item> itens
) {
    public record Item(String nivel, String acao, String confianca, String justificativa, long casos) {}
}
