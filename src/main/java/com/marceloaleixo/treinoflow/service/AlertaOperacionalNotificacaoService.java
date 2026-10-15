package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.IncidenteOperacionalNotificacao;
import com.marceloaleixo.treinoflow.repository.IncidenteOperacionalNotificacaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/** Detecta e persiste incidentes operacionais do canal de notificações no PostgreSQL. */
@Service
public class AlertaOperacionalNotificacaoService {
    private static final Logger log = LoggerFactory.getLogger(AlertaOperacionalNotificacaoService.class);
    private final IncidenteOperacionalNotificacaoRepository repository;
    private final FilaAlertaEmailService filaAlertaEmailService;
    private final String emailDestino;
    private final int limiteFalhasSse;
    private final int janelaMinutos;
    private final Deque<LocalDateTime> falhasSseRecentes = new ArrayDeque<>();

    public AlertaOperacionalNotificacaoService(IncidenteOperacionalNotificacaoRepository repository,
            FilaAlertaEmailService filaAlertaEmailService,
            @Value("${treinoflow.notificacoes.alertas.email.destino:}") String emailDestino,
            @Value("${treinoflow.notificacoes.alertas.limite-falhas-sse:5}") int limiteFalhasSse,
            @Value("${treinoflow.notificacoes.alertas.janela-minutos:10}") int janelaMinutos) {
        this.repository = repository;
        this.filaAlertaEmailService = filaAlertaEmailService;
        this.emailDestino = emailDestino;
        this.limiteFalhasSse = Math.max(2, limiteFalhasSse);
        this.janelaMinutos = Math.max(1, janelaMinutos);
    }

    public synchronized void registrarFalhaSse(Throwable erro) {
        LocalDateTime agora = LocalDateTime.now();
        falhasSseRecentes.addLast(agora);
        LocalDateTime limite = agora.minusMinutes(janelaMinutos);
        while (!falhasSseRecentes.isEmpty() && falhasSseRecentes.peekFirst().isBefore(limite)) {
            falhasSseRecentes.removeFirst();
        }
        if (falhasSseRecentes.size() >= limiteFalhasSse && !existeAberto("FALHAS_SSE")) {
            registrar("FALHAS_SSE", "CRITICO", "Falhas repetidas no canal SSE",
                    falhasSseRecentes.size() + " falhas de entrega em " + janelaMinutos + " minutos. " + mensagem(erro));
        }
    }

    @Transactional
    public synchronized void registrarFalhaRetencao(Throwable erro) {
        if (!existeAberto("RETENCAO_AUDITORIA")) {
            registrar("RETENCAO_AUDITORIA", "ALTO", "Falha na retenção da auditoria",
                    "A limpeza agendada do histórico de preferências não foi concluída. " + mensagem(erro));
        }
    }

    @Transactional
    public synchronized void registrarLimpezaBemSucedida() {
        repository.findByCodigoAndResolvidoFalse("RETENCAO_AUDITORIA")
                .forEach(i -> i.resolver(LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public List<Incidente> listarRecentes() {
        return repository.findTop50ByOrderByOcorridoEmDesc().stream().map(this::paraDto).toList();
    }

    @Transactional(readOnly = true)
    public long contarAbertos() {
        return repository.countByResolvidoFalse();
    }

    @Transactional
    public synchronized boolean resolver(String id) {
        return repository.findByIdAndResolvidoFalse(id).map(i -> {
            i.resolver(LocalDateTime.now());
            return true;
        }).orElse(false);
    }

    private void registrar(String codigo, String severidade, String titulo, String detalhe) {
        String limpo = detalhe == null ? "Detalhe técnico indisponível." : detalhe;
        if (limpo.length() > 1200) limpo = limpo.substring(0, 1199) + "…";
        IncidenteOperacionalNotificacao incidente = repository.save(new IncidenteOperacionalNotificacao(
                UUID.randomUUID().toString(), codigo, severidade, titulo, limpo, LocalDateTime.now()));
        enfileirarEmailDoIncidente(incidente);
    }


    private void enfileirarEmailDoIncidente(IncidenteOperacionalNotificacao incidente) {
        if (emailDestino == null || emailDestino.isBlank()) {
            log.debug("E-mail de alerta operacional não enfileirado: destinatário não configurado.");
            return;
        }
        String assunto = "[TreinoFlow] " + incidente.getSeveridade() + " - " + incidente.getTitulo();
        String corpo = "Um incidente operacional foi registrado no TreinoFlow.\n\n"
                + "Severidade: " + incidente.getSeveridade() + "\n"
                + "Código: " + incidente.getCodigo() + "\n"
                + "Data: " + incidente.getOcorridoEm() + "\n\n"
                + "Detalhes: " + incidente.getDetalhe() + "\n\n"
                + "Acesse o painel administrativo de observabilidade para acompanhar e resolver o incidente.";
        try {
            filaAlertaEmailService.enfileirar(incidente.getId(), emailDestino, assunto, corpo);
        } catch (Exception e) {
            // A falha ao enfileirar não pode apagar o incidente persistido.
            log.warn("Não foi possível enfileirar e-mail do incidente {}: {}", incidente.getId(), mensagem(e));
        }
    }

    private boolean existeAberto(String codigo) {
        return !repository.findByCodigoAndResolvidoFalse(codigo).isEmpty();
    }

    private String mensagem(Throwable erro) {
        if (erro == null || erro.getMessage() == null || erro.getMessage().isBlank()) return "Detalhe técnico indisponível.";
        String detalhe = erro.getMessage().replaceAll("[\\r\\n\\t]+", " ");
        return detalhe.length() > 240 ? detalhe.substring(0, 240) + "…" : detalhe;
    }

    private Incidente paraDto(IncidenteOperacionalNotificacao i) {
        return new Incidente(i.getId(), i.getCodigo(), i.getSeveridade(), i.getTitulo(), i.getDetalhe(),
                i.getOcorridoEm(), i.isResolvido(), i.getResolvidoEm());
    }

    public record Incidente(String id, String codigo, String severidade, String titulo, String detalhe,
                            LocalDateTime ocorridoEm, boolean resolvido, LocalDateTime resolvidoEm) { }
}
