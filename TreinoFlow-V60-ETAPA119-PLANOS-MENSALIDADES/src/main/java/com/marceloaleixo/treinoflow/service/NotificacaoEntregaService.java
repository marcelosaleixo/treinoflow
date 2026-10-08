package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.Notificacao;
import com.marceloaleixo.treinoflow.entity.NotificacaoEntrega;
import com.marceloaleixo.treinoflow.enums.CanalNotificacao;
import java.util.Map;
import com.marceloaleixo.treinoflow.enums.StatusEntregaNotificacao;
import com.marceloaleixo.treinoflow.repository.NotificacaoEntregaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacaoEntregaService {
    private final NotificacaoEntregaRepository entregas;
    private final EmailNotificacaoService email;
    private final WhatsAppNotificacaoService whatsapp;
    private final boolean emailEnabled;
    private final boolean whatsappEnabled;
    private final TemplateNotificacaoService templates;

    public NotificacaoEntregaService(NotificacaoEntregaRepository entregas,
                                     EmailNotificacaoService email,
                                     WhatsAppNotificacaoService whatsapp,
                                     @Value("${treinoflow.notificacoes.email.enabled:false}") boolean emailEnabled,
                                     @Value("${treinoflow.notificacoes.whatsapp.enabled:false}") boolean whatsappEnabled,
                                     TemplateNotificacaoService templates) {
        this.entregas = entregas;
        this.email = email;
        this.whatsapp = whatsapp;
        this.emailEnabled = emailEnabled;
        this.whatsappEnabled = whatsappEnabled;
        this.templates = templates;
    }

    @Transactional
    public void criarEntregas(Notificacao notificacao) {
        if (emailEnabled && notificacao.getPersonal().getEmail() != null && !notificacao.getPersonal().getEmail().isBlank()) {
            criar(notificacao, CanalNotificacao.EMAIL, notificacao.getPersonal().getEmail());
        }
        if (whatsappEnabled && notificacao.getPersonal().getTelefone() != null && !notificacao.getPersonal().getTelefone().isBlank()) {
            criar(notificacao, CanalNotificacao.WHATSAPP, notificacao.getPersonal().getTelefone());
        }
    }

    private void criar(Notificacao notificacao, CanalNotificacao canal, String destino) {
        if (entregas.existsByNotificacaoIdAndCanal(notificacao.getId(), canal)) return;
        NotificacaoEntrega entrega = new NotificacaoEntrega();
        entrega.setNotificacao(notificacao);
        entrega.setCanal(canal);
        entrega.setDestino(destino);
        entrega.setStatus(StatusEntregaNotificacao.PENDENTE);
        entregas.save(entrega);
    }

    @Transactional
    public int processarPendentes() {
        List<NotificacaoEntrega> lista = entregas
                .findTop100ByStatusInAndProximaTentativaLessThanEqualOrderByProximaTentativaAsc(
                        List.of(StatusEntregaNotificacao.PENDENTE, StatusEntregaNotificacao.ERRO), LocalDateTime.now());
        int processadas = 0;
        for (NotificacaoEntrega entrega : lista) {
            if (entrega.getTentativas() >= 5) continue;
            try {
                enviar(entrega);
                entrega.setStatus(StatusEntregaNotificacao.ENVIADO);
                entrega.setDataEnvio(LocalDateTime.now());
                entrega.setUltimoErro(null);
                entregas.save(entrega);
                processadas++;
            } catch (Exception ex) {
                entrega.setTentativas(entrega.getTentativas() + 1);
                entrega.setStatus(StatusEntregaNotificacao.ERRO);
                entrega.setUltimoErro(limitarErro(ex));
                entrega.setProximaTentativa(LocalDateTime.now().plusMinutes(Math.min(60, 5L * entrega.getTentativas())));
                entregas.save(entrega);
            }
        }
        return processadas;
    }

    private void enviar(NotificacaoEntrega entrega) {
        Notificacao n = entrega.getNotificacao();
        Map<String, String> variaveis = variaveis(n);
        TemplateNotificacaoService.Conteudo conteudo = templates.renderizar(
                n.getTipo(), entrega.getCanal(), variaveis, n.getTitulo(), n.getMensagem());
        if (entrega.getCanal() == CanalNotificacao.EMAIL) {
            email.enviar(entrega.getDestino(), conteudo.titulo(), conteudo.mensagem());
        } else if (entrega.getCanal() == CanalNotificacao.WHATSAPP) {
            whatsapp.enviar(entrega.getDestino(), conteudo.mensagem());
        }
    }


    private Map<String, String> variaveis(Notificacao n) {
        if (n.getCobranca() == null) return Map.of("nome", n.getPersonal().getNome() == null ? "" : n.getPersonal().getNome());
        var c = n.getCobranca();
        return Map.of(
                "nome", n.getPersonal().getNome() == null ? "" : n.getPersonal().getNome(),
                "plano", c.getAssinatura().getPlano().getNome() == null ? "" : c.getAssinatura().getPlano().getNome(),
                "valor", c.getValor() == null ? "" : "R$ " + c.getValor().toPlainString().replace('.', ','),
                "vencimento", c.getDataVencimento() == null ? "" : c.getDataVencimento().toString(),
                "link_pagamento", c.getPaymentUrl() == null ? "" : c.getPaymentUrl()
        );
    }
    private String limitarErro(Exception ex) {
        String mensagem = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        return mensagem.length() <= 1000 ? mensagem : mensagem.substring(0, 1000);
    }
}
