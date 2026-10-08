package com.marceloaleixo.treinoflow.enums;

public enum TipoInteracaoCrm {
    CONTATO("Contato"),
    RETENCAO("Retenção"),
    RENOVACAO("Renovação"),
    POS_VENDA("Pós-venda"),
    COBRANCA("Cobrança"),
    OUTRO("Outro");

    private final String descricao;
    TipoInteracaoCrm(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
