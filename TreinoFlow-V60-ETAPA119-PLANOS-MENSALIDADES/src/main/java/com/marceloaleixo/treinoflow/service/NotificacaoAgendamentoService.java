package com.marceloaleixo.treinoflow.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoAgendamentoService {
    private final CobrancaService cobrancas;
    private final NotificacaoService notificacoes;

    public NotificacaoAgendamentoService(CobrancaService cobrancas, NotificacaoService notificacoes) {
        this.cobrancas = cobrancas;
        this.notificacoes = notificacoes;
    }

    @Scheduled(cron = "${treinoflow.notificacoes.cron:0 0 8 * * *}")
    public void executarRotinaDiaria() {
        cobrancas.atualizarAtrasadas();
        notificacoes.gerarAutomaticas();
    }
}
