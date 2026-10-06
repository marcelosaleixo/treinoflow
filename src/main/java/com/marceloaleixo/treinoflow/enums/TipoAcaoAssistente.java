package com.marceloaleixo.treinoflow.enums;

public enum TipoAcaoAssistente {
    WHATSAPP("WhatsApp"),
    FOLLOW_UP("Follow-up");

    private final String descricao;
    TipoAcaoAssistente(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
