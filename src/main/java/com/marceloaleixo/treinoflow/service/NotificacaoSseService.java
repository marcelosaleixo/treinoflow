package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.enums.TipoNotificacao;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Mantém conexões SSE isoladas por personal. Nenhum dado sensível da notificação
 * é enviado pelo canal; o navegador atualiza o contador usando o endpoint autenticado.
 */
@Service
public class NotificacaoSseService {
    private static final long TIMEOUT_MS = 0L;
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> conexoes = new ConcurrentHashMap<>();
    private final PreferenciaNotificacaoService preferencias;
    private final ObservabilidadeNotificacaoService metricas;
    private final AlertaOperacionalNotificacaoService alertas;

    public NotificacaoSseService(PreferenciaNotificacaoService preferencias, ObservabilidadeNotificacaoService metricas,
                                 AlertaOperacionalNotificacaoService alertas) {
        this.preferencias = preferencias;
        this.metricas = metricas;
        this.alertas = alertas;
    }

    public SseEmitter conectar(Long personalId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        CopyOnWriteArrayList<SseEmitter> lista =
                conexoes.computeIfAbsent(personalId, id -> new CopyOnWriteArrayList<>());
        lista.add(emitter);
        metricas.conexaoAberta();
        Runnable remover = () -> remover(personalId, emitter);
        emitter.onCompletion(remover);
        emitter.onTimeout(remover);
        emitter.onError(erro -> remover.run());
        enviar(emitter, "conectado", Map.of("conectado", true));
        return emitter;
    }

    public void publicarAposCommit(Long personalId, TipoNotificacao tipo) {
        if (personalId == null || tipo == null) return;
        org.springframework.transaction.support.TransactionSynchronizationManager
                .registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        if (preferencias.receberEmTempoReal(personalId, tipo)) publicar(personalId);
                    }
                });
    }

    private void publicar(Long personalId) {
        List<SseEmitter> lista = conexoes.get(personalId);
        if (lista == null) return;
        for (SseEmitter emitter : lista) {
            if (enviar(emitter, "notificacao", Map.of("atualizar", true))) metricas.eventoEnviado();
        }
    }

    @Scheduled(fixedRate = 25000)
    public void heartbeat() {
        conexoes.forEach((personalId, lista) -> {
            for (SseEmitter emitter : lista) {
                try {
                    emitter.send(SseEmitter.event().comment("keep-alive"));
                } catch (IOException | IllegalStateException ex) {
                    metricas.falhaEnvio();
                    alertas.registrarFalhaSse(ex);
                    remover(personalId, emitter);
                }
            }
        });
    }

    private boolean enviar(SseEmitter emitter, String evento, Object dados) {
        try {
            emitter.send(SseEmitter.event().name(evento).data(dados));
            return true;
        } catch (IOException | IllegalStateException ex) {
            metricas.falhaEnvio();
            alertas.registrarFalhaSse(ex);
            emitter.completeWithError(ex);
            return false;
        }
    }

    private void remover(Long personalId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> lista = conexoes.get(personalId);
        if (lista != null) {
            if (lista.remove(emitter)) metricas.conexaoFechada();
            if (lista.isEmpty()) conexoes.remove(personalId, lista);
        }
    }
}
