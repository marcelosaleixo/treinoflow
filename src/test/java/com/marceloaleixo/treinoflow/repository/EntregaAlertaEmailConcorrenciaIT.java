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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifica concorrência real no PostgreSQL; executado apenas quando Docker está disponível. */
@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class EntregaAlertaEmailConcorrenciaIT {
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
    void somenteUmTrabalhadorReivindicaAEntrega() throws Exception {
        LocalDateTime agora = LocalDateTime.now().withNano(0);
        repository.saveAndFlush(new EntregaAlertaEmail("concorrencia-1", "incident-conc", "admin@example.com", "Teste concorrência", "Mensagem", agora));
        CountDownLatch largada = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<Integer> primeiro = executor.submit(() -> { largada.await(); return repository.reivindicarAtomico("concorrencia-1", agora, agora.plusMinutes(5)); });
            Future<Integer> segundo = executor.submit(() -> { largada.await(); return repository.reivindicarAtomico("concorrencia-1", agora, agora.plusMinutes(5)); });
            largada.countDown();
            assertEquals(1, primeiro.get() + segundo.get(), "exatamente um trabalhador deve reivindicar a entrega");
        }
        assertEquals("PROCESSANDO", repository.findById("concorrencia-1").orElseThrow().getStatus());
    }
}
