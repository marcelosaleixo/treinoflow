package com.marceloaleixo.treinoflow.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificacaoService {
    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;

    public EmailNotificacaoService(JavaMailSender mailSender,
                                   @Value("${treinoflow.notificacoes.email.enabled:false}") boolean enabled,
                                   @Value("${spring.mail.username:}") String from) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    public void enviar(String destino, String assunto, String mensagem) {
        if (!enabled) throw new IllegalStateException("Envio de e-mail está desativado.");
        if (destino == null || destino.isBlank()) throw new IllegalArgumentException("E-mail do destinatário não informado.");
        SimpleMailMessage mail = new SimpleMailMessage();
        if (from != null && !from.isBlank()) mail.setFrom(from);
        mail.setTo(destino);
        mail.setSubject(assunto);
        mail.setText(mensagem);
        mailSender.send(mail);
    }
}
