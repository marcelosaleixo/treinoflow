package com.marceloaleixo.treinoflow.enums;

public enum StatusCobranca {
    PENDENTE("Pendente"),
    PAGA("Paga"),
    ATRASADA("Atrasada"),
    CANCELADA("Cancelada");

    private final String descricao;
    StatusCobranca(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
