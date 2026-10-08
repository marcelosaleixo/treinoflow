package com.marceloaleixo.treinoflow.dto;

/** Etapa 110: contexto da ação escolhida no Radar Diário. */
public record RadarAcaoView(
        Long alunoId,
        String alunoNome,
        String tipo,
        String titulo,
        String motivo,
        String acao,
        String mensagemSugerida,
        String whatsappUrl
) {}
