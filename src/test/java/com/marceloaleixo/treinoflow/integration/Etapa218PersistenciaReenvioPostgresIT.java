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

/** Etapa 218: confirma no PostgreSQL o estado que o endpoint solicita ao serviço. */
@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class Etapa218PersistenciaReenvioPostgresIT {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configurarPostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired private EntregaAlertaEmailRepository repository;

    @Test
    void reenvioAceitoPersistePendenteERepeticaoNaoMudaEstado() {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        EntregaAlertaEmail entrega = new EntregaAlertaEmail("et218-entrega", "et218-incidente",
                "admin@example.com", "Alerta de teste", "Mensagem de teste", agora);
        repository.saveAndFlush(entrega);
        assertEquals(1, repository.reivindicarAtomico(entrega.getId(), agora, agora.plusMinutes(5)));
        EntregaAlertaEmail processando = repository.findById(entrega.getId()).orElseThrow();
        processando.registrarFalha("Falha SMTP simulada", agora.plusSeconds(1), 1);
        repository.saveAndFlush(processando);

        FilaAlertaEmailService service = new FilaAlertaEmailService(repository, mock(EmailNotificacaoService.class), 4);
        assertTrue(service.reenviar(entrega.getId()));
        assertFalse(service.reenviar(entrega.getId()));

        EntregaAlertaEmail persistida = repository.findById(entrega.getId()).orElseThrow();
        assertEquals("PENDENTE", persistida.getStatus());
        assertEquals(0, persistida.getTentativas());
        assertNull(persistida.getReservadoAte());
        assertNull(persistida.getUltimoErro());
        assertEquals(1, repository.count());
        assertTrue(repository.existsByIncidenteId("et218-incidente"));
    }
}
