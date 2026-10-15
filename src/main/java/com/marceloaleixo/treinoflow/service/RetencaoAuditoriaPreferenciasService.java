package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.repository.PreferenciaNotificacaoAuditoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Retenção configurável do histórico de preferências.
 * A rotina só é ativada explicitamente por configuração; por padrão, nenhum histórico é apagado.
 */
@Service
@ConditionalOnProperty(prefix = "treinoflow.auditoria-preferencias", name = "enabled", havingValue = "true")
public class RetencaoAuditoriaPreferenciasService {
    private static final Logger log = LoggerFactory.getLogger(RetencaoAuditoriaPreferenciasService.class);

    private final PreferenciaNotificacaoAuditoriaRepository repository;
    private final int diasRetencao;
    private final ObservabilidadeNotificacaoService metricas;
    private final AlertaOperacionalNotificacaoService alertas;

    public RetencaoAuditoriaPreferenciasService(
            PreferenciaNotificacaoAuditoriaRepository repository,
            @Value("${treinoflow.auditoria-preferencias.retencao-dias:365}") int diasRetencao,
            ObservabilidadeNotificacaoService metricas,
            AlertaOperacionalNotificacaoService alertas) {
        this.repository = repository;
        this.diasRetencao = Math.max(30, diasRetencao);
        this.metricas = metricas;
        this.alertas = alertas;
    }

    /** Execução diária; habilitada apenas quando a configuração estiver explicitamente ativa. */
    @Scheduled(cron = "${treinoflow.auditoria-preferencias.cron:0 30 3 * * *}")
    @Transactional
    public void executarLimpezaAgendada() {
        int removidos;
        try {
            removidos = excluirRegistrosExpirados();
        } catch (RuntimeException ex) {
            alertas.registrarFalhaRetencao(ex);
            log.error("Falha ao executar retenção da auditoria de preferências.", ex);
            throw ex;
        }
        metricas.registrarLimpeza(removidos);
        alertas.registrarLimpezaBemSucedida();
        if (removidos > 0) {
            log.info("Retenção de auditoria de preferências: {} registros removidos; retenção={} dias.",
                    removidos, diasRetencao);
        } else {
            log.debug("Retenção de auditoria de preferências executada; nenhum registro expirado.");
        }
    }

    @Transactional
    public int excluirRegistrosExpirados() {
        LocalDateTime limite = LocalDateTime.now().minusDays(diasRetencao);
        return repository.excluirAnterioresA(limite);
    }
}
