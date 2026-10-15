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

/** Etapa 202: valida persistência PostgreSQL combinada com entrega SMTP simulada. */
@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class EntregaAlertaEmailE2EPostgresIT {
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
    void persisteEntregaEnviaPorSmtpSimuladoERegistraSucesso() throws Exception {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        EntregaAlertaEmail entrega = new EntregaAlertaEmail(
                "e2e-postgres-smtp-202", "incidente-e2e-202", "admin@example.com",
                "TreinoFlow - alerta de teste", "Mensagem de integração ponta a ponta", agora);
        repository.saveAndFlush(entrega);

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("127.0.0.1");
        sender.setPort(ServerSetupTest.SMTP.getPort());
        EmailNotificacaoService emailService = new EmailNotificacaoService(sender, true, "treinoflow@example.com");

        EntregaAlertaEmail recuperada = repository.findById(entrega.getId()).orElseThrow();
        assertEquals("PENDENTE", recuperada.getStatus());
        emailService.enviar(recuperada.getDestino(), recuperada.getAssunto(), recuperada.getMensagem());

        assertTrue(GREEN_MAIL.waitForIncomingEmail(5000, 1), "SMTP simulado não recebeu a mensagem");
        MimeMessage mensagemRecebida = GREEN_MAIL.getReceivedMessages()[0];
        assertEquals("TreinoFlow - alerta de teste", mensagemRecebida.getSubject());
        assertEquals("admin@example.com", mensagemRecebida.getAllRecipients()[0].toString());

        recuperada.registrarSucesso(LocalDateTime.now().withNano(0));
        repository.saveAndFlush(recuperada);
        EntregaAlertaEmail finalizada = repository.findById(entrega.getId()).orElseThrow();
        assertEquals("ENVIADO", finalizada.getStatus());
        assertNotNull(finalizada.getEnviadaEm());
        assertNull(finalizada.getReservadoAte());
    }

    @Test
    void falhaDeEnvioPodeSerPersistidaParaRetentativa() {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        EntregaAlertaEmail entrega = new EntregaAlertaEmail(
                "e2e-postgres-falha-202", "incidente-e2e-falha-202", "admin@example.com",
                "Teste falha", "Mensagem", agora);
        repository.saveAndFlush(entrega);

        entrega.registrarFalha("Falha SMTP simulada", agora, 4);
        repository.saveAndFlush(entrega);

        EntregaAlertaEmail persistida = repository.findById(entrega.getId()).orElseThrow();
        assertEquals("PENDENTE", persistida.getStatus());
        assertEquals("Falha SMTP simulada", persistida.getUltimoErro());
        assertTrue(persistida.getProximaTentativaEm().isAfter(agora));
    }
}
