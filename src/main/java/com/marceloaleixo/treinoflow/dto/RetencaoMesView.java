package com.marceloaleixo.treinoflow.dto;

public record RetencaoMesView(
        String mes,
        long contatos,
        long recuperados,
        long renovados,
        long cancelamentos,
        double taxaSucesso
) {
    public long resultados() {
        return recuperados + renovados + cancelamentos;
    }

    public int barraSucesso() {
        return (int) Math.round(Math.min(100, Math.max(0, taxaSucesso)));
    }
}
