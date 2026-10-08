package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.RecomendacaoAcaoPerfilView;
import com.marceloaleixo.treinoflow.entity.AcaoAssistente;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente;
import com.marceloaleixo.treinoflow.repository.AcaoAssistenteRepository;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Etapa 114: recomenda o tipo de ação usando o perfil do aluno e o histórico
 * real do próprio Personal. Não executa a ação automaticamente.
 */
@Service
public class RecomendacaoAcaoPerfilService {
    private static final int DIAS_HISTORICO = 180;
    private static final int MIN_RESULTADOS_CONFIAVEIS = 3;

    private final AcaoAssistenteRepository acoes;
    private final AlunoRepository alunos;

    public RecomendacaoAcaoPerfilService(AcaoAssistenteRepository acoes, AlunoRepository alunos) {
        this.acoes = acoes;
        this.alunos = alunos;
    }

    @Transactional(readOnly = true)
    public RecomendacaoAcaoPerfilView recomendar(Long personalId, Long alunoId, int scoreRisco) {
        Aluno aluno = alunos.findByIdAndPersonalId(alunoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este Personal."));

        List<AcaoAssistente> historico = acoes.buscarDesde(personalId, LocalDateTime.now().minusDays(DIAS_HISTORICO));
        int faixaMin = faixaMin(scoreRisco);
        int faixaMax = faixaMax(scoreRisco);
        String objetivo = normalizar(aluno.getObjetivo());
        String idadeFaixa = faixaIdade(aluno.getDataNascimento());

        Grupo exato = grupo(historico, objetivo, idadeFaixa, faixaMin, faixaMax, true);
        RecomendacaoAcaoPerfilView recomendacao = montar(exato, "PERFIL_EXATO");
        if (recomendacao != null) return recomendacao;

        Grupo objetivoRisco = grupo(historico, objetivo, null, faixaMin, faixaMax, false);
        recomendacao = montar(objetivoRisco, "OBJETIVO_RISCO");
        if (recomendacao != null) return recomendacao;

        Grupo risco = grupo(historico, null, null, faixaMin, faixaMax, false);
        recomendacao = montar(risco, "RISCO");
        if (recomendacao != null) return recomendacao;

        Grupo geral = grupo(historico, null, null, 0, 100, false);
        recomendacao = montar(geral, "GERAL");
        if (recomendacao != null) return recomendacao;

        return new RecomendacaoAcaoPerfilView(
                "FOLLOW_UP", "Ainda sem evidência suficiente",
                "Registre mais resultados finais para que o TreinoFlow possa comparar as ações com segurança.",
                0, 0, 0, "GERAL", false,
                "A recomendação é apenas apoio à decisão; o Personal continua responsável pela escolha.",
                0, "Insuficiente", "Não há dados suficientes para calcular uma confiança operacional.",
                "Amostra insuficiente; nenhum padrão confiável identificado."
        );
    }

    private Grupo grupo(List<AcaoAssistente> historico, String objetivo, String idadeFaixa,
                       int min, int max, boolean exigirPerfil) {
        List<AcaoAssistente> filtradas = historico.stream()
                .filter(a -> a.getScoreRisco() >= min && a.getScoreRisco() <= max)
                .filter(a -> !exigirPerfil || normalizar(a.getAluno() == null ? null : a.getAluno().getObjetivo()).equals(objetivo))
                .filter(a -> !exigirPerfil || faixaIdade(a.getAluno() == null ? null : a.getAluno().getDataNascimento()).equals(idadeFaixa))
                .filter(a -> !exigirPerfil || objetivo != null && !objetivo.isBlank())
                .toList();
        return new Grupo(filtradas);
    }

    private RecomendacaoAcaoPerfilView montar(Grupo grupo, String base) {
        if (grupo.acoes().isEmpty()) return null;
        List<Desempenho> desempenhos = java.util.Arrays.stream(TipoAcaoAssistente.values())
                .map(tipo -> desempenho(grupo.acoes(), tipo))
                .filter(d -> d.resultadosFinais() > 0)
                .toList();
        if (desempenhos.isEmpty()) return null;

        Desempenho melhor = desempenhos.stream()
                .max(Comparator.comparingDouble(Desempenho::taxaSucesso)
                        .thenComparingLong(Desempenho::resultadosFinais)
                        .thenComparingLong(Desempenho::total))
                .orElse(null);
        if (melhor == null) return null;

        boolean suficiente = melhor.resultadosFinais() >= MIN_RESULTADOS_CONFIAVEIS;
        String titulo = suficiente ? "Ação com melhor histórico para este contexto" : "Sinal inicial do histórico";
        String motivo = suficiente
                ? melhor.tipo().getDescricao() + " apresentou " + String.format(Locale.US, "%.1f%%", melhor.taxaSucesso())
                    + " de recuperação/renovação entre os resultados finais observados."
                : melhor.tipo().getDescricao() + " aparece à frente no histórico, mas a amostra ainda é pequena.";
        String aviso = suficiente
                ? "Use esta informação como evidência, não como regra automática."
                : "Evite tratar esta indicação como padrão até acumular pelo menos 3 resultados finais.";

        List<Desempenho> ordenados = desempenhos.stream()
                .sorted(Comparator.comparingDouble(Desempenho::taxaSucesso).reversed())
                .toList();
        double segundaTaxa = ordenados.size() > 1 ? ordenados.get(1).taxaSucesso() : melhor.taxaSucesso();
        double margem = Math.max(0, melhor.taxaSucesso() - segundaTaxa);
        int confianca = calcularConfianca(melhor.resultadosFinais(), margem, base);
        String nivel = nivelConfianca(confianca);
        String resumo = "Confiança operacional de " + confianca + "%: " + nivel.toLowerCase(Locale.ROOT) + ".";
        String motivos = montarMotivos(melhor.resultadosFinais(), margem, base);

        return new RecomendacaoAcaoPerfilView(melhor.tipo().name(), titulo, motivo, melhor.taxaSucesso(),
                melhor.resultadosFinais(), melhor.total(), base, suficiente, aviso, confianca, nivel, resumo, motivos);
    }

    private Desempenho desempenho(List<AcaoAssistente> lista, TipoAcaoAssistente tipo) {
        List<AcaoAssistente> grupo = lista.stream().filter(a -> a.getTipoAcao() == tipo).toList();
        long recuperados = contar(grupo, ResultadoCrm.RECUPERADO);
        long renovados = contar(grupo, ResultadoCrm.RENOVADO);
        long finais = grupo.stream().filter(this::resultadoFinal).count();
        double taxa = finais == 0 ? 0 : (recuperados + renovados) * 100.0 / finais;
        return new Desempenho(tipo, grupo.size(), finais, taxa);
    }

    private int calcularConfianca(long resultadosFinais, double margem, String base) {
        // Índice operacional explicável; não representa probabilidade estatística.
        int amostra = (int) Math.min(40, resultadosFinais * 4);
        int separacao = (int) Math.min(40, Math.round(margem * 2));
        int contexto = switch (base) {
            case "PERFIL_EXATO" -> 20;
            case "OBJETIVO_RISCO" -> 15;
            case "RISCO" -> 10;
            default -> 5;
        };
        return Math.min(100, amostra + separacao + contexto);
    }

    private String nivelConfianca(int valor) {
        if (valor >= 80) return "Alta";
        if (valor >= 60) return "Moderada";
        if (valor >= 35) return "Baixa";
        return "Insuficiente";
    }

    private String montarMotivos(long finais, double margem, String base) {
        String amostra = finais >= 10 ? "amostra robusta" : finais >= 5 ? "amostra moderada" : finais >= 3 ? "amostra inicial" : "amostra insuficiente";
        String separacao = margem >= 20 ? "diferença relevante para a segunda estratégia" : margem >= 10 ? "vantagem moderada sobre a segunda estratégia" : "diferença pequena entre as estratégias";
        return amostra + "; " + separacao + "; contexto: " + baseLabel(base) + ".";
    }

    private boolean resultadoFinal(AcaoAssistente a) {
        ResultadoCrm r = a.getResultado();
        return r == ResultadoCrm.RECUPERADO || r == ResultadoCrm.RENOVADO || r == ResultadoCrm.CANCELAMENTO
                || r == ResultadoCrm.SEM_RESPOSTA || r == ResultadoCrm.OUTRO;
    }

    private long contar(List<AcaoAssistente> lista, ResultadoCrm resultado) {
        return lista.stream().filter(a -> a.getResultado() == resultado).count();
    }

    private int faixaMin(int score) {
        if (score >= 75) return 75;
        if (score >= 50) return 50;
        if (score >= 25) return 25;
        return 0;
    }

    private int faixaMax(int score) {
        if (score >= 75) return 100;
        if (score >= 50) return 74;
        if (score >= 25) return 49;
        return 24;
    }

    private String faixaIdade(LocalDate nascimento) {
        if (nascimento == null) return "SEM_IDADE";
        int idade = Period.between(nascimento, LocalDate.now()).getYears();
        if (idade < 25) return "ATE_24";
        if (idade < 35) return "25_34";
        if (idade < 45) return "35_44";
        if (idade < 60) return "45_59";
        return "60_MAIS";
    }

    private String baseLabel(String base) {
        return switch (base == null ? "GERAL" : base) {
            case "PERFIL_EXATO" -> "mesmo perfil + faixa de risco";
            case "OBJETIVO_RISCO" -> "mesmo objetivo + faixa de risco";
            case "RISCO" -> "mesma faixa de risco";
            default -> "histórico geral do Personal";
        };
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }

    private record Grupo(List<AcaoAssistente> acoes) {}
    private record Desempenho(TipoAcaoAssistente tipo, long total, long resultadosFinais, double taxaSucesso) {}
}
