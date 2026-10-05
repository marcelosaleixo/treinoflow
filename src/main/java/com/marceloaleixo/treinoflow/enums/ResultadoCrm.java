package com.marceloaleixo.treinoflow.enums;

public enum ResultadoCrm {
    SEM_RESPOSTA("Sem resposta"),
    EM_ACOMPANHAMENTO("Em acompanhamento"),
    RECUPERADO("Recuperado"),
    RENOVADO("Renovado"),
    CANCELAMENTO("Cancelamento"),
    OUTRO("Outro");

    private final String descricao;
    ResultadoCrm(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
