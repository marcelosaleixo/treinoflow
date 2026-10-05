package com.marceloaleixo.treinoflow.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoEntregaAgendamentoService {
    private final NotificacaoEntregaService entregas;

    public NotificacaoEntregaAgendamentoService(NotificacaoEntregaService entregas) {
        this.entregas = entregas;
    }

    @Scheduled(cron = "${treinoflow.notificacoes.entrega-cron:0 */5 * * * *}")
    public void processar() {
        entregas.processarPendentes();
    }
}
