package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.event.SincronizarMetaReceitaEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Executa a sincronização depois que a alteração financeira foi confirmada no banco. */
@Component
public class SincronizarMetaReceitaEventListener {
    private final UsuarioPersonalService personals;
    private final AlertaMetaReceitaPersistenteService alertas;

    public SincronizarMetaReceitaEventListener(UsuarioPersonalService personals,
            AlertaMetaReceitaPersistenteService alertas) {
        this.personals = personals;
        this.alertas = alertas;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void aoAlterarFinanceiro(SincronizarMetaReceitaEvent evento) {
        if (evento == null || evento.personalId() == null || evento.periodo() == null) return;
        UsuarioPersonal personal = personals.buscarPorId(evento.personalId());
        if (personal != null) alertas.sincronizar(personal, evento.periodo());
    }
}
