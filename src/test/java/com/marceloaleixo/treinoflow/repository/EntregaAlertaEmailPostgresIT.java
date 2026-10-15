package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.EntregaAlertaEmail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Executa contra PostgreSQL real em Docker; não usa H2. */
@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class EntregaAlertaEmailPostgresIT {
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
    void reservaProcessandoPermanecePersistidaERecuperavelAposNovaLeitura() {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        EntregaAlertaEmail original = new EntregaAlertaEmail("postgres-recovery-1", "incident-recovery", "admin@example.com", "Teste recuperação", "Mensagem", agora);
        repository.saveAndFlush(original);
        assertEquals(1, repository.reivindicarAtomico(original.getId(), agora, agora.minusSeconds(1)));

        // Releitura pelo repositório representa a aplicação retomando o estado persistido após reinício.
        EntregaAlertaEmail recuperada = repository.findById(original.getId()).orElseThrow();
        assertEquals("PROCESSANDO", recuperada.getStatus());
        assertEquals(1, recuperada.getTentativas());
        assertEquals(agora.minusSeconds(1), recuperada.getReservadoAte());

        recuperada.recuperarReserva(agora.plusSeconds(2), 4);
        repository.saveAndFlush(recuperada);
        EntregaAlertaEmail aposRecuperacao = repository.findById(original.getId()).orElseThrow();
        assertEquals("PENDENTE", aposRecuperacao.getStatus());
        assertNull(aposRecuperacao.getReservadoAte());
        assertEquals(agora.plusSeconds(2), aposRecuperacao.getProximaTentativaEm());
    }

    @Test
    void reservaExpiradaNaUltimaTentativaVaiParaFalhaFinal() {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        EntregaAlertaEmail entrega = new EntregaAlertaEmail("postgres-recovery-final", "incident-final", "admin@example.com", "Teste final", "Mensagem", agora);
        repository.saveAndFlush(entrega);
        repository.reivindicarAtomico(entrega.getId(), agora, agora.minusSeconds(1));
        EntregaAlertaEmail carregada = repository.findById(entrega.getId()).orElseThrow();
        carregada.recuperarReserva(agora.plusSeconds(1), 1);
        repository.saveAndFlush(carregada);
        assertEquals("FALHA_FINAL", repository.findById(entrega.getId()).orElseThrow().getStatus());
    }

    @Test
    void reivindicacaoCondicionalFuncionaNoPostgresReal() {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        repository.saveAndFlush(new EntregaAlertaEmail("postgres-it-1", "incident-it", "admin@example.com", "Teste", "Mensagem", agora));
        assertEquals(1, repository.reivindicarAtomico("postgres-it-1", agora, agora.plusMinutes(5)));
        assertEquals(0, repository.reivindicarAtomico("postgres-it-1", agora, agora.plusMinutes(5)));
        assertEquals("PROCESSANDO", repository.findById("postgres-it-1").orElseThrow().getStatus());
    }
}
