package com.marceloaleixo.treinoflow.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntregaAlertaEmailTest {
    private final LocalDateTime agora = LocalDateTime.of(2026, 10, 15, 10, 0);

    private EntregaAlertaEmail novaEntrega() {
        return new EntregaAlertaEmail("id-1", "inc-1", "admin@example.com", "Alerta", "Mensagem", agora);
    }

    @Test
    void novaEntregaComecaPendenteEProntaParaProcessar() {
        EntregaAlertaEmail entrega = novaEntrega();
        assertEquals("PENDENTE", entrega.getStatus());
        assertEquals(0, entrega.getTentativas());
        assertEquals(agora, entrega.getProximaTentativaEm());
        assertNull(entrega.getReservadoAte());
    }

    @Test
    void falhaReagendaComIntervaloCrescenteELimitaTextoDoErro() {
        EntregaAlertaEmail entrega = novaEntrega();
        // A tentativa é contabilizada ao reivindicar a entrega, antes do envio SMTP.
        setTentativas(entrega, 1);
        entrega.registrarFalha("x".repeat(1300), agora, 4);
        assertEquals("PENDENTE", entrega.getStatus());
        assertEquals(agora.plusMinutes(2), entrega.getProximaTentativaEm());
        assertEquals(1200, entrega.getUltimoErro().length());
        assertNull(entrega.getReservadoAte());
    }

    @Test
    void falhaNaUltimaTentativaMarcaFalhaFinal() {
        EntregaAlertaEmail entrega = novaEntrega();
        for (int tentativa = 1; tentativa <= 4; tentativa++) {
            // Simula o contador incrementado pela reivindicação atômica no banco.
            setTentativas(entrega, tentativa);
            entrega.registrarFalha("SMTP indisponível", agora.plusMinutes(tentativa), 4);
        }
        assertEquals("FALHA_FINAL", entrega.getStatus());
        assertEquals("SMTP indisponível", entrega.getUltimoErro());
        assertNull(entrega.getReservadoAte());
    }

    @Test
    void sucessoLimpaErroEReserva() {
        EntregaAlertaEmail entrega = novaEntrega();
        setTentativas(entrega, 1);
        entrega.registrarFalha("Falha transitória", agora, 4);
        entrega.registrarSucesso(agora.plusMinutes(2));
        assertEquals("ENVIADO", entrega.getStatus());
        assertEquals(agora.plusMinutes(2), entrega.getEnviadaEm());
        assertNull(entrega.getUltimoErro());
        assertNull(entrega.getReservadoAte());
    }

    @Test
    void reservaExpiradaDevolveParaFilaOuFinalizaConformeLimite() {
        EntregaAlertaEmail recuperavel = novaEntrega();
        setTentativas(recuperavel, 2);
        recuperavel.recuperarReserva(agora.plusMinutes(5), 4);
        assertEquals("PENDENTE", recuperavel.getStatus());
        assertEquals(agora.plusMinutes(5), recuperavel.getProximaTentativaEm());
        assertNull(recuperavel.getReservadoAte());

        EntregaAlertaEmail esgotada = novaEntrega();
        setTentativas(esgotada, 4);
        esgotada.recuperarReserva(agora.plusMinutes(5), 4);
        assertEquals("FALHA_FINAL", esgotada.getStatus());
        assertTrue(esgotada.getUltimoErro().contains("reserva expirou"));
    }

    @Test
    void reenvioManualLimpaEstadoAnterior() {
        EntregaAlertaEmail entrega = novaEntrega();
        setTentativas(entrega, 4);
        entrega.registrarFalha("Falha permanente", agora, 4);
        entrega.reabrirParaReenvio(agora.plusHours(1));
        assertEquals("PENDENTE", entrega.getStatus());
        assertEquals(0, entrega.getTentativas());
        assertEquals(agora.plusHours(1), entrega.getProximaTentativaEm());
        assertNull(entrega.getUltimoErro());
        assertNull(entrega.getEnviadaEm());
    }

    private void setTentativas(EntregaAlertaEmail entrega, int tentativas) {
        try {
            var campo = EntregaAlertaEmail.class.getDeclaredField("tentativas");
            campo.setAccessible(true);
            campo.setInt(entrega, tentativas);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Não foi possível preparar o estado de teste", e);
        }
    }
}
