package com.marceloaleixo.treinoflow.enums;

public enum CanalCrm {
    WHATSAPP("WhatsApp"),
    EMAIL("E-mail"),
    TELEFONE("Telefone"),
    INTERNO("Interno");

    private final String descricao;
    CanalCrm(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
