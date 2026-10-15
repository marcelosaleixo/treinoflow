package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.EntregaAlertaEmail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Testes de integração JPA com H2 em modo compatível com PostgreSQL. */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:treinoflow_integration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false"
})
@ActiveProfiles("integration")
class EntregaAlertaEmailRepositoryIntegrationTest {
    @Autowired
    private EntregaAlertaEmailRepository repository;

    @Test
    void persisteEntregaEContaStatus() {
        LocalDateTime agora = LocalDateTime.of(2026, 10, 15, 10, 0);
        repository.saveAndFlush(new EntregaAlertaEmail("mail-1", "inc-1", "admin@example.com", "Alerta", "Mensagem", agora));
        assertEquals(1, repository.countByStatus("PENDENTE"));
        EntregaAlertaEmail salvo = repository.findById("mail-1").orElseThrow();
        assertEquals("admin@example.com", salvo.getDestino());
        assertEquals("PENDENTE", salvo.getStatus());
    }

    @Test
    void reivindicacaoAtomicaReservaUmaEntregaApenasUmaVez() {
        LocalDateTime agora = LocalDateTime.of(2026, 10, 15, 10, 0);
        repository.saveAndFlush(new EntregaAlertaEmail("mail-2", "inc-2", "admin@example.com", "Alerta", "Mensagem", agora));
        assertEquals(1, repository.reivindicarAtomico("mail-2", agora, agora.plusMinutes(5)));
        assertEquals(0, repository.reivindicarAtomico("mail-2", agora, agora.plusMinutes(5)));
        EntregaAlertaEmail reservada = repository.findById("mail-2").orElseThrow();
        assertEquals("PROCESSANDO", reservada.getStatus());
        assertEquals(1, reservada.getTentativas());
        assertEquals(agora.plusMinutes(5), reservada.getReservadoAte());
    }

    @Test
    void pesquisaPaginadaRespeitaStatusEIntervalo() {
        LocalDateTime agora = LocalDateTime.of(2026, 10, 15, 10, 0);
        repository.saveAndFlush(new EntregaAlertaEmail("mail-3", "inc-3", "a@example.com", "A", "A", agora.minusDays(2)));
        repository.saveAndFlush(new EntregaAlertaEmail("mail-4", "inc-4", "b@example.com", "B", "B", agora));
        var pagina = repository.pesquisar("PENDENTE", agora.minusDays(1), agora.plusDays(1),
                org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(1, pagina.getTotalElements());
        assertEquals("mail-4", pagina.getContent().get(0).getId());
    }
}
