package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.PainelExecutivoRetencaoView;
import com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PainelExecutivoRetencaoService {
    private final AlunoRepository alunos;
    private final InteracaoCrmRepository interacoes;
    private final UsuarioPersonalService usuarioPersonalService;
    private final ScoreRiscoAlunoService scoreService;

    public PainelExecutivoRetencaoService(AlunoRepository alunos,
                                          InteracaoCrmRepository interacoes,
                                          UsuarioPersonalService usuarioPersonalService,
                                          ScoreRiscoAlunoService scoreService) {
        this.alunos = alunos;
        this.interacoes = interacoes;
        this.usuarioPersonalService = usuarioPersonalService;
        this.scoreService = scoreService;
    }

    @Transactional(readOnly = true)
    public PainelExecutivoRetencaoView dashboard(Long personalId) {
        UsuarioPersonal personal = usuarioPersonalService.buscarPorId(personalId);
        long ativos = alunos.findByPersonalIdAndStatusOrderByNomeAsc(personal.getId(), "ATIVO").size();
        List<ScoreRiscoAlunoView> riscos = scoreService.listar(personal.getId());

        long criticos = riscos.stream().filter(r -> "CRÍTICO".equalsIgnoreCase(r.nivel())).count();
        long altos = riscos.stream().filter(r -> "ALTO".equalsIgnoreCase(r.nivel())).count();
        long medios = riscos.stream().filter(r -> "MÉDIO".equalsIgnoreCase(r.nivel())).count();
        long baixos = riscos.stream().filter(r -> "BAIXO".equalsIgnoreCase(r.nivel())).count();
        long semContato = interacoes.countAlunosSemContatoDesde(personal.getId(), java.time.LocalDateTime.now().minusDays(30));
        long followUps = interacoes.countAcoesPendentes(personal.getId(), LocalDate.now(), ResultadoCrm.EM_ACOMPANHAMENTO);
        long recuperados = interacoes.countAlunosDistinctPorResultado(personal.getId(), ResultadoCrm.RECUPERADO)
                + interacoes.countAlunosDistinctPorResultado(personal.getId(), ResultadoCrm.RENOVADO);
        long cancelamentos = interacoes.countAlunosDistinctPorResultado(personal.getId(), ResultadoCrm.CANCELAMENTO);
        long baseResultado = recuperados + cancelamentos;
        double taxa = baseResultado == 0 ? 0D : recuperados * 100D / baseResultado;
        long emRisco = criticos + altos + medios;
        double percentualRisco = ativos == 0 ? 0D : emRisco * 100D / ativos;

        return new PainelExecutivoRetencaoView(ativos, criticos, altos, medios, baixos,
                semContato, followUps, recuperados, cancelamentos, taxa, percentualRisco);
    }
}
