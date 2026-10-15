package com.marceloaleixo.treinoflow.integration;

import com.marceloaleixo.treinoflow.entity.EntregaAlertaEmail;
import com.marceloaleixo.treinoflow.repository.EntregaAlertaEmailRepository;
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

/** Etapa 208: verifica persistência e recuperação de reservas expiradas no PostgreSQL. */
@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class Etapa208RecuperacaoFilaPostgresIT {
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
    void reservaExpiradaRetornaParaPendenteEContinuaPersistida() {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        repository.saveAndFlush(new EntregaAlertaEmail("et208-recuperar", "incidente-et208-recuperar",
                "admin@example.com", "Alerta", "Teste recuperação", agora));

        assertEquals(1, repository.reivindicarAtomico("et208-recuperar", agora, agora.plusMinutes(5)));
        repository.flush();

        LocalDateTime depoisDaExpiracao = agora.plusMinutes(6);
        EntregaAlertaEmail reservada = repository.findTop100ByStatusAndReservadoAteLessThanEqualOrderByReservadoAteAsc(
                "PROCESSANDO", depoisDaExpiracao).stream().filter(e -> e.getId().equals("et208-recuperar")).findFirst().orElseThrow();
        reservada.recuperarReserva(depoisDaExpiracao, 4);
        repository.saveAndFlush(reservada);

        EntregaAlertaEmail recuperada = repository.findById("et208-recuperar").orElseThrow();
        assertEquals("PENDENTE", recuperada.getStatus());
        assertNull(recuperada.getReservadoAte());
        assertEquals(depoisDaExpiracao, recuperada.getProximaTentativaEm());
        assertTrue(recuperada.getUltimoErro().contains("devolvida à fila"));
    }

    @Test
    void reservaExpiradaAposLimiteVaiParaFalhaFinal() {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        repository.saveAndFlush(new EntregaAlertaEmail("et208-final", "incidente-et208-final",
                "admin@example.com", "Alerta", "Teste limite", agora));
        assertEquals(1, repository.reivindicarAtomico("et208-final", agora, agora.plusMinutes(5)));
        repository.flush();

        EntregaAlertaEmail reservada = repository.findTop100ByStatusAndReservadoAteLessThanEqualOrderByReservadoAteAsc(
                "PROCESSANDO", agora.plusMinutes(6)).stream().filter(e -> e.getId().equals("et208-final")).findFirst().orElseThrow();
        reservada.recuperarReserva(agora.plusMinutes(6), 1);
        repository.saveAndFlush(reservada);

        EntregaAlertaEmail finalizada = repository.findById("et208-final").orElseThrow();
        assertEquals("FALHA_FINAL", finalizada.getStatus());
        assertNull(finalizada.getReservadoAte());
        assertTrue(finalizada.getUltimoErro().contains("reserva expirou"));
    }
}
