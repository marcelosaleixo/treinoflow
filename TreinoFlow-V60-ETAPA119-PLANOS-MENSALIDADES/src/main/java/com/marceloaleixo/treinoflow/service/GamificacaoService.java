package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.GamificacaoDashboardView;
import com.marceloaleixo.treinoflow.dto.MetaComercialDashboardView;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.repository.RegistroTreinoAlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class GamificacaoService {
    private final MetaComercialService metaComercialService;
    private final InteracaoCrmRepository interacoes;
    private final RegistroTreinoAlunoRepository registros;

    public GamificacaoService(MetaComercialService metaComercialService,
                              InteracaoCrmRepository interacoes,
                              RegistroTreinoAlunoRepository registros) {
        this.metaComercialService = metaComercialService;
        this.interacoes = interacoes;
        this.registros = registros;
    }

    @Transactional(readOnly = true)
    public GamificacaoDashboardView dashboard(Long personalId) {
        MetaComercialDashboardView meta = metaComercialService.dashboard(personalId);

        int pontosMetas = pontosPorIndicador(meta.percentualAlunos(), meta.percentualTreinos(), meta.percentualRecuperacoes());
        int pontosTreinos = (int) Math.min(100, meta.treinosRealizados() / 5);
        int pontosRecuperacoes = (int) Math.min(100, meta.recuperacoes() * 10);
        int pontosCarteira = (int) Math.min(100, meta.alunosAtivos() * 2);
        int pontos = pontosMetas + pontosTreinos + pontosRecuperacoes + pontosCarteira;

        List<String> conquistas = new ArrayList<>();
        if (meta.progressoGeral() >= 100) conquistas.add("🏆 Meta geral do mês atingida");
        if (meta.percentualRecuperacoes() >= 100 && meta.metaRecuperacoes() > 0) conquistas.add("🔄 Recuperador do mês");
        if (meta.percentualTreinos() >= 100) conquistas.add("🏋️ Constância de execução");
        if (meta.percentualAlunos() >= 100) conquistas.add("👥 Carteira em meta");
        if (meta.progressoGeral() >= 75) conquistas.add("🔥 Ritmo de alta performance");
        if (conquistas.isEmpty()) conquistas.add("🚀 Primeira conquista: avance suas metas este mês");

        Nivel nivel = nivel(pontos);
        int progresso = progressoNivel(pontos, nivel);
        String mensagem = switch (nivel.nome) {
            case "Elite" -> "Excelente desempenho. Mantenha a consistência e proteja sua carteira.";
            case "Alta performance" -> "Você está em ritmo forte. Concentre pontos no indicador mais distante da meta.";
            case "Em evolução" -> "Boa evolução. Cada treino acompanhado e cada aluno recuperado aumenta sua pontuação.";
            default -> "Comece pelas metas do mês. Pequenas ações consistentes fazem sua pontuação subir.";
        };

        return new GamificacaoDashboardView(meta.mesLabel(), pontos, nivel.nome, nivel.proximo,
                nivel.limiteProximo, progresso, pontosMetas, pontosTreinos, pontosRecuperacoes,
                pontosCarteira, conquistas, mensagem);
    }

    private int pontosPorIndicador(int alunos, int treinos, int recuperacoes) {
        int pontos = 0;
        pontos += alunos >= 100 ? 50 : alunos >= 75 ? 30 : alunos >= 50 ? 15 : 0;
        pontos += treinos >= 100 ? 50 : treinos >= 75 ? 30 : treinos >= 50 ? 15 : 0;
        pontos += recuperacoes >= 100 ? 50 : recuperacoes >= 75 ? 30 : recuperacoes >= 50 ? 15 : 0;
        return pontos;
    }

    private Nivel nivel(int pontos) {
        if (pontos >= 500) return new Nivel("Elite", "Elite", 500);
        if (pontos >= 250) return new Nivel("Alta performance", "Elite", 500);
        if (pontos >= 100) return new Nivel("Em evolução", "Alta performance", 250);
        return new Nivel("Iniciante", "Em evolução", 100);
    }

    private int progressoNivel(int pontos, Nivel nivel) {
        if ("Elite".equals(nivel.nome)) return 100;
        int inicio = switch (nivel.nome) {
            case "Alta performance" -> 250;
            case "Em evolução" -> 100;
            default -> 0;
        };
        int faixa = nivel.limiteProximo - inicio;
        return faixa <= 0 ? 100 : Math.min(100, Math.max(0, Math.round((pontos - inicio) * 100f / faixa)));
    }

    private record Nivel(String nome, String proximo, int limiteProximo) {}
}
