package com.marceloaleixo.treinoflow.integration;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.marceloaleixo.treinoflow.entity.EntregaAlertaEmail;
import com.marceloaleixo.treinoflow.repository.EntregaAlertaEmailRepository;
import com.marceloaleixo.treinoflow.service.EmailNotificacaoService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Etapa 203: valida o ciclo persistência -> SMTP -> estado final persistido. */
@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class FluxoCompletoEntregaAlertaEmailIT {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @RegisterExtension
    static final GreenMailExtension GREEN_MAIL = new GreenMailExtension(ServerSetupTest.SMTP);

    @DynamicPropertySource
    static void configurarPostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired
    private EntregaAlertaEmailRepository repository;

    @Test
    void percorreCicloCompletoDePendenteAEnviado() throws Exception {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        EntregaAlertaEmail entrega = new EntregaAlertaEmail(
                "e2e-completo-203", "incidente-completo-203", "admin@example.com",
                "TreinoFlow - incidente operacional", "Falha simulada para validar o fluxo completo.", agora);

        repository.saveAndFlush(entrega);
        assertEquals("PENDENTE", repository.findById(entrega.getId()).orElseThrow().getStatus());

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("127.0.0.1");
        sender.setPort(ServerSetupTest.SMTP.getPort());
        EmailNotificacaoService emailService = new EmailNotificacaoService(sender, true, "treinoflow@example.com");

        EntregaAlertaEmail reservada = repository.findById(entrega.getId()).orElseThrow();
        emailService.enviar(reservada.getDestino(), reservada.getAssunto(), reservada.getMensagem());
        assertTrue(GREEN_MAIL.waitForIncomingEmail(5000, 1), "GreenMail não recebeu o alerta");

        MimeMessage recebida = GREEN_MAIL.getReceivedMessages()[0];
        assertEquals("admin@example.com", recebida.getAllRecipients()[0].toString());
        assertEquals("TreinoFlow - incidente operacional", recebida.getSubject());

        reservada.registrarSucesso(LocalDateTime.now().withNano(0));
        repository.saveAndFlush(reservada);

        EntregaAlertaEmail finalizada = repository.findById(entrega.getId()).orElseThrow();
        assertEquals("ENVIADO", finalizada.getStatus());
        assertNotNull(finalizada.getEnviadaEm());
        assertNull(finalizada.getReservadoAte());
        assertNull(finalizada.getUltimoErro());
    }
}
