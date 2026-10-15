package com.marceloaleixo.treinoflow.integration;

import com.marceloaleixo.treinoflow.entity.EntregaAlertaEmail;
import com.marceloaleixo.treinoflow.repository.EntregaAlertaEmailRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/** Etapa 217: concorrência no reenvio administrativo com bloqueio pessimista no PostgreSQL. */
@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class Etapa217ReenvioConcorrenciaPostgresIT {
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
    @Autowired PlatformTransactionManager transactionManager;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void somenteUmaSolicitacaoConcorrenteReabreFalhaFinal() throws Exception {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        EntregaAlertaEmail entrega = new EntregaAlertaEmail("et217-concorrente", "incidente-et217-concorrente",
                "admin@example.com", "Alerta", "Mensagem", agora);
        repository.saveAndFlush(entrega);

        // Simula uma entrega que atingiu o limite de tentativas e ficou em falha definitiva.
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            EntregaAlertaEmail atual = repository.buscarParaAtualizacao(entrega.getId()).orElseThrow();
            assertEquals(1, repository.reivindicarAtomico(entrega.getId(), agora, agora.plusMinutes(5)));
            atual = repository.buscarParaAtualizacao(entrega.getId()).orElseThrow();
            atual.registrarFalha("SMTP indisponível", agora.plusSeconds(1), 1);
            repository.saveAndFlush(atual);
        });
        assertEquals("FALHA_FINAL", repository.findById(entrega.getId()).orElseThrow().getStatus());

        CountDownLatch largada = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<Boolean> primeira = executor.submit(() -> { largada.await(); return reenviarEmTransacao(entrega.getId()); });
            Future<Boolean> segunda = executor.submit(() -> { largada.await(); return reenviarEmTransacao(entrega.getId()); });
            largada.countDown();
            boolean resultado1 = primeira.get();
            boolean resultado2 = segunda.get();
            assertNotEquals(resultado1, resultado2,
                    "uma única solicitação deve reabrir a entrega; a concorrente deve encontrar o estado já alterado");
        }

        EntregaAlertaEmail finalizada = repository.findById(entrega.getId()).orElseThrow();
        assertEquals("PENDENTE", finalizada.getStatus());
        assertEquals(0, finalizada.getTentativas());
        assertNull(finalizada.getReservadoAte());
        assertNull(finalizada.getUltimoErro());
        assertEquals(1, repository.count());
        assertTrue(repository.existsByIncidenteId("incidente-et217-concorrente"));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void solicitacoesRepetidasDepoisDaReaberturaSaoRecusadas() {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        EntregaAlertaEmail entrega = new EntregaAlertaEmail("et217-repetida", "incidente-et217-repetida",
                "admin@example.com", "Alerta", "Mensagem", agora);
        repository.saveAndFlush(entrega);
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            EntregaAlertaEmail atual = repository.buscarParaAtualizacao(entrega.getId()).orElseThrow();
            assertEquals(1, repository.reivindicarAtomico(entrega.getId(), agora, agora.plusMinutes(5)));
            atual = repository.buscarParaAtualizacao(entrega.getId()).orElseThrow();
            atual.registrarFalha("SMTP indisponível", agora.plusSeconds(1), 1);
            repository.saveAndFlush(atual);
        });

        assertTrue(reenviarEmTransacao(entrega.getId()));
        assertFalse(reenviarEmTransacao(entrega.getId()));
        assertFalse(reenviarEmTransacao(entrega.getId()));
        assertEquals(1, repository.count());
    }

    private boolean reenviarEmTransacao(String id) {
        TransactionTemplate transacao = new TransactionTemplate(transactionManager);
        transacao.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return Boolean.TRUE.equals(transacao.execute(status -> {
            EntregaAlertaEmail entrega = repository.buscarParaAtualizacao(id).orElse(null);
            if (entrega == null || !"FALHA_FINAL".equals(entrega.getStatus())) return false;
            entrega.reabrirParaReenvio(LocalDateTime.now());
            repository.saveAndFlush(entrega);
            return true;
        }));
    }
}
