package com.marceloaleixo.treinoflow.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Etapa 123: visão operacional da cobrança por prioridade e momento do vencimento. */
public record CobrancaInteligenteView(
        Long contaId,
        Long alunoId,
        String alunoNome,
        String telefone,
        BigDecimal valor,
        LocalDate vencimento,
        long diasParaVencer,
        String status,
        String prioridade,
        String titulo,
        String acaoSugerida,
        String mensagemSugerida,
        String whatsappUrl,
        boolean acompanhamentoPendente
) {}
