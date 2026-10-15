package com.marceloaleixo.treinoflow.integration;

import com.marceloaleixo.treinoflow.entity.EntregaAlertaEmail;
import com.marceloaleixo.treinoflow.repository.EntregaAlertaEmailRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Etapa 207: valida idempotência e reivindicação condicional no PostgreSQL real. */
@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class Etapa207FilaAlertasPostgresIT {
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
    void restricaoUnicaImpedeDuasEntregasParaMesmoIncidente() {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        repository.saveAndFlush(new EntregaAlertaEmail("et207-a", "incidente-et207", "admin@example.com", "Alerta", "Teste", agora));
        assertTrue(repository.existsByIncidenteId("incidente-et207"));

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(
                new EntregaAlertaEmail("et207-b", "incidente-et207", "admin@example.com", "Alerta duplicado", "Teste", agora)));
    }

    @Test
    void apenasUmaReivindicacaoCondicionalConsegueReservarEntrega() {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        repository.saveAndFlush(new EntregaAlertaEmail("et207-claim", "incidente-et207-claim", "admin@example.com", "Alerta", "Teste", agora));

        int primeira = repository.reivindicarAtomico("et207-claim", agora, agora.plusMinutes(5));
        int segunda = repository.reivindicarAtomico("et207-claim", agora, agora.plusMinutes(5));

        assertEquals(1, primeira, "A primeira reivindicação deve reservar a entrega");
        assertEquals(0, segunda, "A entrega já PROCESSANDO não pode ser reivindicada novamente");
        assertEquals("PROCESSANDO", repository.findById("et207-claim").orElseThrow().getStatus());
    }
}
