package com.marceloaleixo.treinoflow.enums;

public enum StatusAssinatura {
    EM_TESTE("Em teste"), ATIVA("Ativa"), INADIMPLENTE("Inadimplente"), VENCIDA("Vencida"), SUSPENSA("Suspensa"), CANCELADA("Cancelada");
    private final String descricao;
    StatusAssinatura(String descricao){this.descricao=descricao;}
    public String getDescricao(){return descricao;}
}
