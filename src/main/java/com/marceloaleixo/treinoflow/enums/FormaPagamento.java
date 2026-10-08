package com.marceloaleixo.treinoflow.enums;

public enum FormaPagamento {
    PIX("PIX"),
    DINHEIRO("Dinheiro"),
    CARTAO("Cartão"),
    TRANSFERENCIA("Transferência"),
    BOLETO("Boleto"),
    OUTRO("Outro");

    private final String descricao;
    FormaPagamento(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
