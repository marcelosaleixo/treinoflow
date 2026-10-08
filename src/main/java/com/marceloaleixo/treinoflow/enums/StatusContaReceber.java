package com.marceloaleixo.treinoflow.enums;

public enum StatusContaReceber {
    PENDENTE("Pendente"),
    PAGA("Paga"),
    ATRASADA("Atrasada"),
    CANCELADA("Cancelada");

    private final String descricao;
    StatusContaReceber(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
