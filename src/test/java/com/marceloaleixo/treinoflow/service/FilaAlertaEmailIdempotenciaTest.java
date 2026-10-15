package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.EntregaAlertaEmail;
import com.marceloaleixo.treinoflow.repository.EntregaAlertaEmailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FilaAlertaEmailIdempotenciaTest {
    private EntregaAlertaEmailRepository repository;
    private FilaAlertaEmailService service;

    @BeforeEach
    void configurar() {
        repository = mock(EntregaAlertaEmailRepository.class);
        service = new FilaAlertaEmailService(repository, mock(EmailNotificacaoService.class), 4);
    }

    @Test
    void chamadasRepetidasParaMesmoIncidenteCriamUmaUnicaEntrega() {
        when(repository.existsByIncidenteId("inc-123")).thenReturn(false, true);

        service.enfileirar("inc-123", "admin@example.com", "Alerta", "Detalhes");
        service.enfileirar("inc-123", "admin@example.com", "Alerta", "Detalhes");

        verify(repository, times(1)).saveAndFlush(any(EntregaAlertaEmail.class));
        verify(repository, times(2)).existsByIncidenteId("inc-123");
    }

    @Test
    void rejeitaIdentificadorDeIncidenteVazio() {
        assertThrows(IllegalArgumentException.class,
                () -> service.enfileirar(" ", "admin@example.com", "Alerta", "Detalhes"));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void naoCriaEntregaQuandoDestinatarioEstaVazio() {
        service.enfileirar("inc-123", " ", "Alerta", "Detalhes");
        verifyNoInteractions(repository);
    }

    @Test
    void conflitoUnicoConcorrenteETratadoComoDuplicidadeSeRegistroJaExiste() {
        when(repository.existsByIncidenteId("inc-123")).thenReturn(false, true);
        when(repository.saveAndFlush(any(EntregaAlertaEmail.class)))
                .thenThrow(new DataIntegrityViolationException("uk_entrega_email_incidente"));

        assertDoesNotThrow(() -> service.enfileirar("inc-123", "admin@example.com", "Alerta", "Detalhes"));
        verify(repository).saveAndFlush(any(EntregaAlertaEmail.class));
        verify(repository, times(2)).existsByIncidenteId("inc-123");
    }

    @Test
    void propagaViolacaoDeIntegridadeQuandoNaoExisteRegistroConcorrente() {
        when(repository.existsByIncidenteId("inc-123")).thenReturn(false, false);
        when(repository.saveAndFlush(any(EntregaAlertaEmail.class)))
                .thenThrow(new DataIntegrityViolationException("erro de integridade"));

        assertThrows(DataIntegrityViolationException.class,
                () -> service.enfileirar("inc-123", "admin@example.com", "Alerta", "Detalhes"));
    }
}
