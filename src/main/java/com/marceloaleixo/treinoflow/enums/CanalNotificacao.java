package com.marceloaleixo.treinoflow.enums;

public enum CanalNotificacao {
    INTERNA("Notificação no sistema"),
    EMAIL("E-mail"),
    WHATSAPP("WhatsApp");

    private final String descricao;

    CanalNotificacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
