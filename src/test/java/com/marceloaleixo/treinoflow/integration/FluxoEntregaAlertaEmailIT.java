package com.marceloaleixo.treinoflow.integration;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.marceloaleixo.treinoflow.entity.EntregaAlertaEmail;
import com.marceloaleixo.treinoflow.service.EmailNotificacaoService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Fluxo de integração do domínio de entrega com SMTP local simulado. */
class FluxoEntregaAlertaEmailIT {
    @RegisterExtension
    static final GreenMailExtension GREEN_MAIL = new GreenMailExtension(ServerSetupTest.SMTP);

    @Test
    void entregaDeAlertaSaiDaFilaERegistraSucessoAposAceiteSmtp() throws Exception {
        LocalDateTime agora = LocalDateTime.now();
        EntregaAlertaEmail entrega = new EntregaAlertaEmail(
                "it-entrega-1", "it-incidente-1", "admin@example.com",
                "TreinoFlow: incidente crítico", "Falha operacional simulada", agora);

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("127.0.0.1");
        sender.setPort(ServerSetupTest.SMTP.getPort());
        EmailNotificacaoService emailService = new EmailNotificacaoService(sender, true, "treinoflow@example.com");

        assertEquals("PENDENTE", entrega.getStatus());
        emailService.enviar(entrega.getDestino(), entrega.getAssunto(), entrega.getMensagem());
        assertTrue(GREEN_MAIL.waitForIncomingEmail(5000, 1), "SMTP simulado não recebeu a mensagem");

        MimeMessage recebido = GREEN_MAIL.getReceivedMessages()[0];
        assertEquals(entrega.getAssunto(), recebido.getSubject());
        assertEquals(entrega.getDestino(), recebido.getAllRecipients()[0].toString());

        entrega.registrarSucesso(LocalDateTime.now());
        assertEquals("ENVIADO", entrega.getStatus());
        assertNotNull(entrega.getEnviadaEm());
        assertNull(entrega.getReservadoAte());
    }

    @Test
    void falhaAntesDoEnvioMantemEntregaReagendada() {
        LocalDateTime agora = LocalDateTime.now();
        EntregaAlertaEmail entrega = new EntregaAlertaEmail(
                "it-entrega-2", "it-incidente-2", "admin@example.com",
                "Alerta de teste", "Mensagem", agora);

        entrega.registrarFalha("SMTP indisponível", agora, 4);

        assertEquals("PENDENTE", entrega.getStatus());
        assertEquals("SMTP indisponível", entrega.getUltimoErro());
        assertNotNull(entrega.getUltimaTentativaEm());
        assertTrue(entrega.getProximaTentativaEm().isAfter(agora));
    }

    @Test
    void falhaNoLimiteDeTentativasVaiParaFalhaFinal() {
        LocalDateTime agora = LocalDateTime.now();
        EntregaAlertaEmail entrega = new EntregaAlertaEmail(
                "it-entrega-3", "it-incidente-3", "admin@example.com",
                "Alerta de teste", "Mensagem", agora);
        for (int i = 0; i < 4; i++) entrega.registrarFalha("SMTP indisponível", agora.plusMinutes(i), 0);
        assertEquals("FALHA_FINAL", entrega.getStatus());
    }
}
