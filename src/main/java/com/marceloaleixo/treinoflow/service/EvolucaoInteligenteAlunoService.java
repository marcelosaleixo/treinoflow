package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.AnaliseMultiSerieView;
import com.marceloaleixo.treinoflow.dto.AnaliseTendenciaProgressaoView;
import com.marceloaleixo.treinoflow.dto.EvolucaoInteligenteAlunoView;
import com.marceloaleixo.treinoflow.dto.PrescricaoAuditoriaView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.Treino;
import com.marceloaleixo.treinoflow.entity.TreinoExercicio;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.TreinoExercicioRepository;
import com.marceloaleixo.treinoflow.repository.TreinoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Etapa 86 - consolida as inteligências das etapas 83-85 em um painel por aluno.
 * Apenas recomenda; nunca altera a prescrição do Personal.
 */
@Service
public class EvolucaoInteligenteAlunoService {
    private final AlunoRepository alunos;
    private final TreinoRepository treinos;
    private final TreinoExercicioRepository itens;
    private final TendenciaProgressaoService tendencia;
    private final AnaliseMultiSerieService multiSerie;
    private final ProgressaoInteligenteService progressao;
    private final PrescricaoAssistidaService prescricao;
    private final com.marceloaleixo.treinoflow.repository.PrescricaoAuditoriaRepository auditorias;

    public EvolucaoInteligenteAlunoService(AlunoRepository alunos,
                                           TreinoRepository treinos,
                                           TreinoExercicioRepository itens,
                                           TendenciaProgressaoService tendencia,
                                           AnaliseMultiSerieService multiSerie,
                                           ProgressaoInteligenteService progressao,
                                           PrescricaoAssistidaService prescricao,
                                           com.marceloaleixo.treinoflow.repository.PrescricaoAuditoriaRepository auditorias) {
        this.alunos = alunos;
        this.treinos = treinos;
        this.itens = itens;
        this.tendencia = tendencia;
        this.multiSerie = multiSerie;
        this.progressao = progressao;
        this.prescricao = prescricao;
        this.auditorias = auditorias;
    }

    @Transactional(readOnly = true)
    public EvolucaoInteligenteAlunoView montar(Long alunoId, Long personalId) {
        Aluno aluno = alunos.findByIdAndPersonalId(alunoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este Personal."));

        List<Treino> liberados = treinos.buscarLiberadosDoPortal(alunoId, LocalDateTime.now());
        Treino treinoReferencia = liberados.isEmpty()
                ? treinos.findByAlunoIdOrderByDataCriacaoDesc(alunoId).stream().findFirst().orElse(null)
                : liberados.get(0);

        if (treinoReferencia == null) {
            return new EvolucaoInteligenteAlunoView(
                    aluno.getId(), aluno.getNome(), aluno.getObjetivo(), null,
                    0, "SEM_DADOS", "Este aluno ainda não possui treino cadastrado para gerar uma análise de evolução.",
                    0, 0, 0, 0, 0, 0, List.of());
        }

        List<TreinoExercicio> exercicios = itens.listarComExercicioPorTreino(treinoReferencia.getId());
        List<EvolucaoInteligenteAlunoView.ExercicioEvolucaoView> analises = new ArrayList<>();

        for (TreinoExercicio item : exercicios) {
            AnaliseTendenciaProgressaoView t = tendencia.analisar(item, aluno);
            AnaliseMultiSerieView m = multiSerie.analisar(item, aluno);
            var p = progressao.recomendar(item, aluno);
            var pa = prescricao.sugerir(item, aluno);

            String nivel = normalizarNivel(t.nivel(), m.nivel(), p.nivel());
            String titulo = titulo(nivel, t, m, p);
            String resumo = resumo(nivel, t, m, p);
            String acao = t.acao() != null ? t.acao() : m.detalhe();
            String ultima = p.ultimaExecucao();
            Double variacao = m.variacaoVolumePercentual() != null ? m.variacaoVolumePercentual() : t.variacaoVolumePercentual();
            Double rpe = m.rpeMedioUltimaSessao() != null ? m.rpeMedioUltimaSessao() : t.rpeMedioUltimaSessao();

            String grupo = item.getExercicio() != null && item.getExercicio().getGrupoMuscular() != null
                    ? item.getExercicio().getGrupoMuscular().name().replace('_', ' ')
                    : "—";
            analises.add(new EvolucaoInteligenteAlunoView.ExercicioEvolucaoView(
                    item.getId(),
                    item.getExercicio() == null ? "Exercício" : item.getExercicio().getNome(),
                    grupo,
                    nivel,
                    t.confianca(),
                    titulo,
                    resumo,
                    acao,
                    ultima,
                    variacao,
                    rpe,
                    t.scoreTendencia(),
                    pa));
        }

        long progressaoCount = analises.stream().filter(a -> "PROGRESSAO".equals(a.nivel())).count();
        long atencaoCount = analises.stream().filter(a -> "ATENCAO".equals(a.nivel())).count();
        long fadigaCount = analises.stream().filter(a -> "FADIGA".equals(a.nivel())).count();
        long semDadosCount = analises.stream().filter(a -> "SEM_DADOS".equals(a.nivel())).count();
        long estavelCount = analises.size() - progressaoCount - atencaoCount - fadigaCount - semDadosCount;

        double score = analises.stream()
                .map(EvolucaoInteligenteAlunoView.ExercicioEvolucaoView::scoreTendencia)
                .filter(v -> v != null)
                .mapToDouble(Double::doubleValue)
                .average().orElse(0D);

        String nivelGeral;
        String resumoGeral;
        if (analises.isEmpty()) {
            nivelGeral = "SEM_DADOS";
            resumoGeral = "O treino de referência ainda não possui exercícios para análise.";
        } else if (atencaoCount + fadigaCount > progressaoCount && atencaoCount + fadigaCount > 0) {
            nivelGeral = "ATENCAO";
            resumoGeral = "Há exercícios que pedem cautela. Revise fadiga, recuperação e execução antes de buscar novas progressões.";
        } else if (progressaoCount > 0 && progressaoCount >= estavelCount) {
            nivelGeral = "PROGRESSAO";
            resumoGeral = "A carteira de exercícios apresenta sinais positivos de evolução. Avalie as recomendações de alta confiança antes da próxima prescrição.";
        } else {
            nivelGeral = "ESTAVEL";
            resumoGeral = "O desempenho está predominantemente estável. Continue registrando as séries para aumentar a confiança das análises.";
        }

        analises.sort(Comparator.comparingInt(this::ordemNivel).thenComparing(EvolucaoInteligenteAlunoView.ExercicioEvolucaoView::nome));
        return new EvolucaoInteligenteAlunoView(
                aluno.getId(), aluno.getNome(), aluno.getObjetivo(), treinoReferencia.getNome(),
                score, nivelGeral, resumoGeral, analises.size(),
                (int) progressaoCount, (int) estavelCount, (int) atencaoCount,
                (int) fadigaCount, (int) semDadosCount, analises);
    }

    @Transactional(readOnly = true)
    public List<PrescricaoAuditoriaView> historicoPrescricoes(Long alunoId, Long personalId) {
        alunos.findByIdAndPersonalId(alunoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este Personal."));
        return auditorias.buscarRecentesDoAluno(alunoId, personalId).stream()
                .limit(30)
                .map(PrescricaoAuditoriaView::from)
                .toList();
    }

    private int ordemNivel(EvolucaoInteligenteAlunoView.ExercicioEvolucaoView v) {
        return switch (v.nivel()) {
            case "ATENCAO" -> 0;
            case "FADIGA" -> 1;
            case "PROGRESSAO" -> 2;
            case "ESTAVEL" -> 3;
            default -> 4;
        };
    }

    private String normalizarNivel(String tendencia, String multi, String progressao) {
        if ("ATENCAO".equals(tendencia) || "ATENCAO".equals(multi) || "ATENCAO".equals(progressao)) return "ATENCAO";
        if ("FADIGA".equals(multi) || "QUEDA".equals(multi)) return "FADIGA";
        if ("PROGRESSAO".equals(tendencia) || "PROGRESSAO".equals(multi) || "PROGREDIR".equals(progressao)) return "PROGRESSAO";
        if ("SEM_DADOS".equals(tendencia) || "SEM_DADOS".equals(multi)) return "SEM_DADOS";
        return "ESTAVEL";
    }

    private String titulo(String nivel, AnaliseTendenciaProgressaoView t, AnaliseMultiSerieView m, com.marceloaleixo.treinoflow.dto.RecomendacaoProgressaoView p) {
        return switch (nivel) {
            case "ATENCAO" -> "Requer atenção";
            case "FADIGA" -> m.titulo();
            case "PROGRESSAO" -> t.titulo();
            case "SEM_DADOS" -> "Sem dados suficientes";
            default -> m.titulo();
        };
    }

    private String resumo(String nivel, AnaliseTendenciaProgressaoView t, AnaliseMultiSerieView m, com.marceloaleixo.treinoflow.dto.RecomendacaoProgressaoView p) {
        return switch (nivel) {
            case "ATENCAO", "FADIGA" -> m.resumo();
            case "PROGRESSAO" -> t.resumo();
            case "SEM_DADOS" -> t.resumo();
            default -> m.resumo();
        };
    }
}
