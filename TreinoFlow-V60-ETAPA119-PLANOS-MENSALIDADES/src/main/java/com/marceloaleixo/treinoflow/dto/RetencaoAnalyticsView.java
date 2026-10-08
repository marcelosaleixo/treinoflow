package com.marceloaleixo.treinoflow.dto;

import java.util.List;

public record RetencaoAnalyticsView(
        long alunosAtivos,
        long alunosEmRisco,
        long contatos30Dias,
        long recuperados30Dias,
        long renovados30Dias,
        long cancelamentos30Dias,
        double taxaSucesso30Dias,
        double taxaCancelamento30Dias,
        double tempoMedioRecuperacaoDias,
        List<RetencaoMesView> meses,
        List<MotivoRiscoView> motivosRisco,
        List<AlunoResultadoView> recuperadosRecentes,
        List<AlunoResultadoView> renovadosRecentes
) {
    public long resultados30Dias() {
        return recuperados30Dias + renovados30Dias + cancelamentos30Dias;
    }
}
