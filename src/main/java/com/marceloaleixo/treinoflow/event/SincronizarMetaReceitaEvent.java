package com.marceloaleixo.treinoflow.event;

import java.time.YearMonth;

/** Evento financeiro que solicita a atualização dos alertas de meta de um personal e período. */
public record SincronizarMetaReceitaEvent(Long personalId, YearMonth periodo) {
}
