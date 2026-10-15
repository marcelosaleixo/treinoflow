package com.marceloaleixo.treinoflow.integration;

import com.marceloaleixo.treinoflow.entity.EntregaAlertaEmail;
import com.marceloaleixo.treinoflow.repository.EntregaAlertaEmailRepository;
import com.marceloaleixo.treinoflow.service.EmailNotificacaoService;
import com.marceloaleixo.treinoflow.service.FilaAlertaEmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/** Etapa 216: verifica as regras de reenvio com estado persistido no PostgreSQL. */
@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class Etapa216ReenvioRegrasFilaPostgresIT {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configurarPostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired EntregaAlertaEmailRepository repository;

    @Test
    void permiteReabrirSomenteEntregaEmFalhaFinalEPersisteEstadoPendente() {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        EntregaAlertaEmail entrega = new EntregaAlertaEmail("et216-final", "incidente-et216-final",
                "admin@example.com", "Alerta", "Mensagem", agora);
        repository.saveAndFlush(entrega);

        assertEquals(1, repository.reivindicarAtomico(entrega.getId(), agora, agora.plusMinutes(5)));
        EntregaAlertaEmail processando = repository.findById(entrega.getId()).orElseThrow();
        processando.registrarFalha("SMTP indisponível", agora.plusSeconds(1), 1);
        repository.saveAndFlush(processando);
        assertEquals("FALHA_FINAL", repository.findById(entrega.getId()).orElseThrow().getStatus());

        FilaAlertaEmailService service = new FilaAlertaEmailService(repository, mock(EmailNotificacaoService.class), 4);
        assertTrue(service.reenviar(entrega.getId()));

        EntregaAlertaEmail reaberta = repository.findById(entrega.getId()).orElseThrow();
        assertEquals("PENDENTE", reaberta.getStatus());
        assertEquals(0, reaberta.getTentativas());
        assertNull(reaberta.getReservadoAte());
        assertNull(reaberta.getUltimoErro());
        assertNull(reaberta.getEnviadaEm());
        assertFalse(reaberta.getProximaTentativaEm().isBefore(agora));
    }

    @Test
    void recusaReenvioDeEntregaPendenteSemAlterarEstado() {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        EntregaAlertaEmail entrega = new EntregaAlertaEmail("et216-pendente", "incidente-et216-pendente",
                "admin@example.com", "Alerta", "Mensagem", agora);
        repository.saveAndFlush(entrega);

        FilaAlertaEmailService service = new FilaAlertaEmailService(repository, mock(EmailNotificacaoService.class), 4);
        assertFalse(service.reenviar(entrega.getId()));

        EntregaAlertaEmail mantida = repository.findById(entrega.getId()).orElseThrow();
        assertEquals("PENDENTE", mantida.getStatus());
        assertEquals(0, mantida.getTentativas());
        assertNull(mantida.getReservadoAte());
    }

    @Test
    void idempotenciaMantemUmaEntregaPorIncidenteMesmoAposReabertura() {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        EntregaAlertaEmail entrega = new EntregaAlertaEmail("et216-idempotente", "incidente-et216-idempotente",
                "admin@example.com", "Alerta", "Mensagem", agora);
        repository.saveAndFlush(entrega);

        FilaAlertaEmailService service = new FilaAlertaEmailService(repository, mock(EmailNotificacaoService.class), 4);
        assertFalse(service.reenviar(entrega.getId()), "Entrega pendente não deve ser reaberta");
        assertEquals(1, repository.count());
        assertTrue(repository.existsByIncidenteId("incidente-et216-idempotente"));
    }
}
