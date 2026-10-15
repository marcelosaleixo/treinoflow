package com.marceloaleixo.treinoflow.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/** Métricas operacionais em memória; não armazena conteúdo nem identificadores pessoais. */
@Service
public class ObservabilidadeNotificacaoService {
    private final AtomicLong conexoesAtivas = new AtomicLong();
    private final LongAdder eventosEnviados = new LongAdder();
    private final LongAdder falhasEnvio = new LongAdder();
    private volatile LocalDateTime ultimaLimpeza;
    private volatile Integer registrosRemovidosUltimaLimpeza;

    public void conexaoAberta() { conexoesAtivas.incrementAndGet(); }
    public void conexaoFechada() { conexoesAtivas.updateAndGet(v -> Math.max(0, v - 1)); }
    public void eventoEnviado() { eventosEnviados.increment(); }
    public void falhaEnvio() { falhasEnvio.increment(); }
    public void registrarLimpeza(int removidos) {
        ultimaLimpeza = LocalDateTime.now();
        registrosRemovidosUltimaLimpeza = removidos;
    }
    public Resumo obterResumo() {
        return new Resumo(conexoesAtivas.get(), eventosEnviados.sum(), falhasEnvio.sum(),
                ultimaLimpeza, registrosRemovidosUltimaLimpeza);
    }
    public record Resumo(long conexoesSseAtivas, long eventosEnviados, long falhasEnvio,
                         LocalDateTime ultimaLimpezaAuditoria, Integer registrosRemovidosUltimaLimpeza) {}
}
