package com.marceloaleixo.treinoflow.dto;

import com.marceloaleixo.treinoflow.entity.InteracaoCrm;

import java.time.LocalDate;

/** Etapa 108: visão operacional de um follow-up pendente. */
public record FollowUpInteligenteView(
        Long interacaoId,
        Long alunoId,
        String alunoNome,
        String canal,
        String tipo,
        String assunto,
        String descricao,
        LocalDate dataProximaAcao,
        String statusPrazo,
        String prioridade,
        String acaoSugerida,
        long diasAtraso,
        String whatsappUrl
) {
    public static FollowUpInteligenteView from(InteracaoCrm i, String statusPrazo,
                                                String prioridade, String acaoSugerida,
                                                long diasAtraso, String whatsappUrl) {
        return new FollowUpInteligenteView(
                i.getId(), i.getAluno().getId(), i.getAluno().getNome(),
                i.getCanal() == null ? "—" : i.getCanal().getDescricao(),
                i.getTipo() == null ? "—" : i.getTipo().getDescricao(),
                i.getAssunto(), i.getDescricao(), i.getDataProximaAcao(),
                statusPrazo, prioridade, acaoSugerida, diasAtraso, whatsappUrl
        );
    }
}
