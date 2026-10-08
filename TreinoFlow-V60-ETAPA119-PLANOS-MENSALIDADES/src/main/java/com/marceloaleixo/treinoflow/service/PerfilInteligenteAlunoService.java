package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.PerfilInteligenteAlunoView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.RegistroTreinoAluno;
import com.marceloaleixo.treinoflow.repository.RegistroTreinoAlunoRepository;
import com.marceloaleixo.treinoflow.repository.TreinoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class PerfilInteligenteAlunoService {
    private final RegistroTreinoAlunoRepository registros;
    private final TreinoRepository treinos;
    private final ScoreRiscoAlunoService riscoService;

    public PerfilInteligenteAlunoService(RegistroTreinoAlunoRepository registros, TreinoRepository treinos, ScoreRiscoAlunoService riscoService) {
        this.registros = registros;
        this.treinos = treinos;
        this.riscoService = riscoService;
    }

    @Transactional(readOnly = true)
    public PerfilInteligenteAlunoView montar(Aluno aluno) {
        LocalDate hoje = LocalDate.now();
        Integer idade = aluno.getIdade();
        String faixa = faixaEtaria(idade);

        List<RegistroTreinoAluno> historico = registros.buscarHistorico(aluno.getId());
        List<RegistroTreinoAluno> concluidos = historico.stream().filter(RegistroTreinoAluno::isConcluido).toList();
        long sessoes30 = concluidos.stream()
                .filter(r -> r.getDataExecucao() != null && !r.getDataExecucao().isBefore(hoje.minusDays(29)))
                .count();
        LocalDate ultimaSessao = concluidos.stream()
                .map(RegistroTreinoAluno::getDataExecucao)
                .filter(d -> d != null)
                .findFirst().orElse(null);

        long treinosCadastrados = treinos.countByAlunoId(aluno.getId());
        long treinosLiberados = treinos.countByAlunoIdAndStatus(aluno.getId(), "LIBERADO");
        String engajamento = nivelEngajamento(sessoes30, ultimaSessao, hoje);
        String perfil = construirPerfil(aluno, idade, engajamento, sessoes30);
        List<String> sinais = sinais(aluno, idade, treinosCadastrados, treinosLiberados, sessoes30, ultimaSessao, hoje);
        String resumo = construirResumo(aluno, idade, engajamento, sessoes30, treinosCadastrados, ultimaSessao);
        int frequenciaScore = scoreFrequencia(sessoes30);
        int recenciaScore = scoreRecencia(ultimaSessao, hoje);
        int continuidadeScore = scoreContinuidade(concluidos, hoje);
        int execucaoScore = scoreExecucao(historico, hoje);
        int aderenciaScore = calcularAderencia(frequenciaScore, recenciaScore, continuidadeScore, execucaoScore);
        String aderenciaNivel = nivelAderencia(aderenciaScore);
        var risco = riscoService.listar(aluno.getPersonal().getId()).stream()
                .filter(item -> aluno.getId().equals(item.alunoId()))
                .findFirst()
                .orElse(null);

        int riscoScore = risco == null ? 0 : risco.score();
        String riscoNivel = risco == null ? "BAIXO" : risco.nivel();
        int radarRisco = 100 - riscoScore;
        int radarEngajamento = scoreEngajamento(engajamento);
        int radarEvolucao = calcularRadarEvolucao(aderenciaScore, recenciaScore, execucaoScore);
        int radarScore = (int) Math.round(aderenciaScore * 0.35 + radarEvolucao * 0.25
                + radarEngajamento * 0.20 + radarRisco * 0.20);
        String radarNivel = nivelRadar(radarScore);
        String radarAcao = acaoRadar(radarScore, riscoScore, aderenciaScore, radarEngajamento);

        return new PerfilInteligenteAlunoView(
                faixa, idade, textoOuPadrao(aluno.getObjetivo(), "Não informado"), aluno.getStatus(),
                tempoRelacionamento(aluno.getDataInicio(), hoje), treinosCadastrados, treinosLiberados,
                concluidos.size(), sessoes30, ultimaSessao, engajamento, perfil, resumo, sinais,
                aderenciaScore, aderenciaNivel, frequenciaScore, recenciaScore, continuidadeScore, execucaoScore,
                riscoScore, riscoNivel,
                risco == null ? "Manter acompanhamento." : risco.acaoRecomendada(),
                risco == null ? java.util.List.of("Não foi possível calcular o risco de abandono no momento.") : risco.sinais(),
                aderenciaScore, radarEvolucao, radarEngajamento, radarRisco, radarScore, radarNivel, radarAcao
        );
    }

    private String construirPerfil(Aluno aluno, Integer idade, String engajamento, long sessoes30) {
        String objetivo = aluno.getObjetivo() == null ? "" : aluno.getObjetivo().toLowerCase();
        if ("ALTO".equals(engajamento) && objetivo.contains("hipertrof")) return "Alta consistência · Hipertrofia";
        if ("ALTO".equals(engajamento) && objetivo.contains("emag")) return "Alta consistência · Emagrecimento";
        if ("ALTO".equals(engajamento)) return "Alta consistência de treino";
        if ("MODERADO".equals(engajamento)) return "Consistência moderada";
        if (sessoes30 == 0) return "Baixa atividade recente";
        if (idade != null && idade >= 60) return "Perfil 60+ · atenção à individualização";
        return "Perfil em construção";
    }

    private String construirResumo(Aluno aluno, Integer idade, String engajamento, long sessoes30,
                                   long treinosCadastrados, LocalDate ultimaSessao) {
        String idadeTexto = idade == null ? "idade não informada" : idade + " anos";
        String objetivo = textoOuPadrao(aluno.getObjetivo(), "objetivo não informado");
        String ultima = ultimaSessao == null ? "sem sessão registrada" : "última sessão em " + ultimaSessao;
        return "Aluno de " + idadeTexto + ", objetivo " + objetivo + ". "
                + "Foram observadas " + sessoes30 + " sessões nos últimos 30 dias, "
                + treinosCadastrados + " treino(s) cadastrado(s) e " + ultima + ". "
                + "Engajamento atual: " + engajamento.toLowerCase() + ".";
    }

    private List<String> sinais(Aluno aluno, Integer idade, long treinos, long liberados,
                                long sessoes30, LocalDate ultima, LocalDate hoje) {
        List<String> sinais = new ArrayList<>();
        if (idade == null) sinais.add("Cadastre a data de nascimento para personalizar o perfil por idade.");
        if (aluno.getObjetivo() == null || aluno.getObjetivo().isBlank()) sinais.add("Defina o objetivo principal do aluno.");
        if (treinos == 0) sinais.add("Ainda não há treino cadastrado para este aluno.");
        else if (liberados == 0) sinais.add("Há treino cadastrado, mas nenhum treino está liberado no momento.");
        if (sessoes30 == 0) sinais.add("Nenhuma sessão concluída nos últimos 30 dias; vale verificar a continuidade do acompanhamento.");
        else if (sessoes30 < 4) sinais.add("Ritmo recente abaixo de uma sessão por semana em média; acompanhe a aderência.");
        if (ultima != null && ultima.isBefore(hoje.minusDays(14))) sinais.add("A última sessão registrada ocorreu há mais de 14 dias.");
        if (sinais.isEmpty()) sinais.add("Dados atuais indicam um perfil consistente para continuar o acompanhamento.");
        return sinais;
    }


    private int scoreFrequencia(long sessoes30) {
        return (int) Math.min(100, Math.round((sessoes30 / 12.0) * 100));
    }

    private int scoreRecencia(LocalDate ultima, LocalDate hoje) {
        if (ultima == null || ultima.isAfter(hoje)) return 0;
        long dias = ChronoUnit.DAYS.between(ultima, hoje);
        if (dias <= 3) return 100;
        if (dias <= 7) return 90;
        if (dias <= 14) return 70;
        if (dias <= 21) return 40;
        if (dias <= 30) return 20;
        return 0;
    }

    private int scoreContinuidade(List<RegistroTreinoAluno> concluidos, LocalDate hoje) {
        long semanasAtivas = concluidos.stream()
                .map(RegistroTreinoAluno::getDataExecucao)
                .filter(d -> d != null && !d.isBefore(hoje.minusDays(27)) && !d.isAfter(hoje))
                .map(d -> ChronoUnit.WEEKS.between(hoje.minusDays(27), d))
                .distinct()
                .count();
        return (int) Math.min(100, Math.round((semanasAtivas / 4.0) * 100));
    }

    private int scoreExecucao(List<RegistroTreinoAluno> historico, LocalDate hoje) {
        List<RegistroTreinoAluno> periodo = historico.stream()
                .filter(r -> r.getDataExecucao() != null
                        && !r.getDataExecucao().isBefore(hoje.minusDays(29))
                        && !r.getDataExecucao().isAfter(hoje))
                .toList();
        if (periodo.isEmpty()) return 0;
        long concluidos = periodo.stream().filter(RegistroTreinoAluno::isConcluido).count();
        return (int) Math.round((concluidos * 100.0) / periodo.size());
    }

    private int calcularAderencia(int frequencia, int recencia, int continuidade, int execucao) {
        return (int) Math.round(frequencia * 0.30 + recencia * 0.30 + continuidade * 0.20 + execucao * 0.20);
    }

    private String nivelAderencia(int score) {
        if (score >= 80) return "EXCELENTE";
        if (score >= 60) return "BOA";
        if (score >= 40) return "MODERADA";
        return "BAIXA";
    }

    private String nivelEngajamento(long sessoes30, LocalDate ultima, LocalDate hoje) {
        if (ultima == null || sessoes30 == 0) return "BAIXO";
        long dias = ChronoUnit.DAYS.between(ultima, hoje);
        if (sessoes30 >= 8 && dias <= 7) return "ALTO";
        if (sessoes30 >= 4 && dias <= 14) return "MODERADO";
        return "BAIXO";
    }

    private String faixaEtaria(Integer idade) {
        if (idade == null) return "Não informada";
        if (idade < 18) return "Menor de 18";
        if (idade < 30) return "18–29 anos";
        if (idade < 40) return "30–39 anos";
        if (idade < 50) return "40–49 anos";
        if (idade < 60) return "50–59 anos";
        return "60+ anos";
    }

    private String tempoRelacionamento(LocalDate inicio, LocalDate hoje) {
        if (inicio == null) return "Não informado";
        Period p = Period.between(inicio, hoje);
        if (p.isNegative()) return "Data inválida";
        if (p.getYears() > 0) return p.getYears() + (p.getYears() == 1 ? " ano" : " anos");
        if (p.getMonths() > 0) return p.getMonths() + (p.getMonths() == 1 ? " mês" : " meses");
        return Math.max(0, p.getDays()) + " dias";
    }

    private int scoreEngajamento(String nivel) {
        return switch (nivel) {
            case "ALTO" -> 100;
            case "MODERADO" -> 70;
            default -> 35;
        };
    }

    private int calcularRadarEvolucao(int aderencia, int recencia, int execucao) {
        return (int) Math.round(aderencia * 0.45 + recencia * 0.30 + execucao * 0.25);
    }

    private String nivelRadar(int score) {
        if (score >= 80) return "EXCELENTE";
        if (score >= 60) return "BOM";
        if (score >= 40) return "ATENÇÃO";
        return "CRÍTICO";
    }

    private String acaoRadar(int score, int risco, int aderencia, int engajamento) {
        if (risco >= 75 || score < 40) return "Priorizar este aluno hoje: investigar a queda de aderência e realizar contato preventivo.";
        if (risco >= 50 || aderencia < 60) return "Acompanhar de perto e considerar uma ação preventiva se os sinais persistirem.";
        if (engajamento < 70) return "Reforçar o acompanhamento e estimular a retomada da rotina.";
        return "Aluno consistente: manter acompanhamento e continuar registrando evolução.";
    }

    private String textoOuPadrao(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor;
    }
}
