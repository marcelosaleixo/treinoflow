package com.marceloaleixo.treinoflow.service;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifica entrega SMTP contra servidor local simulado, sem enviar e-mail externo. */
class EmailNotificacaoGreenMailIT {
    @RegisterExtension
    static final GreenMailExtension GREEN_MAIL = new GreenMailExtension(ServerSetupTest.SMTP);

    @Test
    void enviaMensagemParaServidorSmtpSimulado() throws Exception {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("127.0.0.1");
        sender.setPort(ServerSetupTest.SMTP.getPort());
        EmailNotificacaoService service = new EmailNotificacaoService(sender, true, "treinoflow@example.com");

        service.enviar("admin@example.com", "Alerta de teste", "Mensagem de teste de integração.");

        assertTrue(GREEN_MAIL.waitForIncomingEmail(5000, 1));
        MimeMessage recebido = GREEN_MAIL.getReceivedMessages()[0];
        assertEquals("Alerta de teste", recebido.getSubject());
        assertEquals("admin@example.com", recebido.getAllRecipients()[0].toString());
    }
}
