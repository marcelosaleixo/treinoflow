package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.RetencaoResultadoView;
import com.marceloaleixo.treinoflow.dto.RetencaoResultadosDashboardView;
import com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.CanalCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RetencaoResultadosService {
    private final InteracaoCrmRepository interacoes;
    private final AlunoRepository alunos;
    private final UsuarioPersonalService usuarioPersonalService;
    private final ScoreRiscoAlunoService scoreService;

    public RetencaoResultadosService(InteracaoCrmRepository interacoes,
                                     AlunoRepository alunos,
                                     UsuarioPersonalService usuarioPersonalService,
                                     ScoreRiscoAlunoService scoreService) {
        this.interacoes = interacoes;
        this.alunos = alunos;
        this.usuarioPersonalService = usuarioPersonalService;
        this.scoreService = scoreService;
    }

    @Transactional(readOnly = true)
    public RetencaoResultadosDashboardView dashboard(Long personalId, int dias) {
        List<InteracaoCrm> lista = retencoesNoPeriodo(personalId, dias);
        long acoes = lista.size();
        long contatos = lista.stream().filter(i -> i.getCanal() != CanalCrm.INTERNO).count();
        long respostas = lista.stream().filter(this::consideradaResposta).count();
        long recuperados = lista.stream().filter(i -> i.getResultado() == ResultadoCrm.RECUPERADO || i.getResultado() == ResultadoCrm.RENOVADO).map(i -> i.getAluno().getId()).distinct().count();
        long cancelamentos = lista.stream().filter(i -> i.getResultado() == ResultadoCrm.CANCELAMENTO).map(i -> i.getAluno().getId()).distinct().count();
        long emAcompanhamento = lista.stream().filter(i -> i.getResultado() == ResultadoCrm.EM_ACOMPANHAMENTO).count();
        long base = recuperados + cancelamentos;
        double taxa = base == 0 ? 0D : recuperados * 100D / base;
        return new RetencaoResultadosDashboardView(acoes, contatos, respostas, recuperados, cancelamentos, emAcompanhamento, taxa);
    }

    @Transactional(readOnly = true)
    public List<RetencaoResultadoView> listar(Long personalId, int dias, String filtroNivel) {
        Map<Long, ScoreRiscoAlunoView> riscos = scoreService.listar(personalId).stream()
                .collect(Collectors.toMap(ScoreRiscoAlunoView::alunoId, Function.identity()));
        return retencoesNoPeriodo(personalId, dias).stream()
                .sorted(Comparator.comparing(InteracaoCrm::getDataContato).reversed())
                .map(i -> {
                    ScoreRiscoAlunoView risco = riscos.get(i.getAluno().getId());
                    if (risco == null) return null;
                    if (filtroNivel != null && !filtroNivel.isBlank() && !risco.nivel().equalsIgnoreCase(filtroNivel)) return null;
                    return new RetencaoResultadoView(
                            risco.alunoId(), risco.nome(), risco.score(), risco.nivel(), risco.getNivelCss(),
                            i.getResultado(), i.getResultado().getDescricao(), i.getDataContato(), i.getAssunto());
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional
    public void registrarResultado(Long personalId, Long alunoId, ResultadoCrm resultado) {
        if (resultado == null || resultado == ResultadoCrm.EM_ACOMPANHAMENTO) {
            throw new IllegalArgumentException("Selecione um resultado final da ação.");
        }
        Aluno aluno = alunos.findByIdAndPersonalId(alunoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este personal."));
        UsuarioPersonal personal = usuarioPersonalService.buscarPorId(personalId);

        InteracaoCrm interacao = new InteracaoCrm();
        interacao.setPersonal(personal);
        interacao.setAluno(aluno);
        interacao.setCanal(CanalCrm.INTERNO);
        interacao.setTipo(TipoInteracaoCrm.RETENCAO);
        interacao.setResultado(resultado);
        interacao.setAssunto("Resultado da ação de retenção");
        interacao.setDescricao(descricaoResultado(resultado));
        interacao.setDataProximaAcao(null);
        interacoes.save(interacao);
    }

    private List<InteracaoCrm> retencoesNoPeriodo(Long personalId, int dias) {
        int periodo = dias == 90 ? 90 : 30;
        return interacoes.buscarRetencoesDesde(personalId, LocalDateTime.now().minusDays(periodo));
    }

    private boolean consideradaResposta(InteracaoCrm i) {
        return i.getResultado() == ResultadoCrm.RECUPERADO
                || i.getResultado() == ResultadoCrm.RENOVADO
                || i.getResultado() == ResultadoCrm.CANCELAMENTO
                || i.getResultado() == ResultadoCrm.OUTRO;
    }

    private String descricaoResultado(ResultadoCrm resultado) {
        return switch (resultado) {
            case RECUPERADO -> "Aluno marcado como recuperado após ação de retenção.";
            case RENOVADO -> "Aluno renovou após ação de retenção.";
            case CANCELAMENTO -> "Aluno marcado como cancelamento após ação de retenção.";
            case SEM_RESPOSTA -> "Ação realizada sem resposta do aluno.";
            case OUTRO -> "Resultado registrado manualmente na central de retenção.";
            case EM_ACOMPANHAMENTO -> "Acompanhamento em andamento.";
        };
    }
}
