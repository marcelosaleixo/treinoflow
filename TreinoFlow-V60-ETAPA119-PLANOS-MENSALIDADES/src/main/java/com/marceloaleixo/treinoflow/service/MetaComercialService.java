package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.MetaComercialDashboardView;
import com.marceloaleixo.treinoflow.entity.MetaComercial;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.repository.MetaComercialRepository;
import com.marceloaleixo.treinoflow.repository.RegistroTreinoAlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
public class MetaComercialService {
    private final MetaComercialRepository metas;
    private final AlunoRepository alunos;
    private final RegistroTreinoAlunoRepository registros;
    private final InteracaoCrmRepository interacoes;
    private final UsuarioPersonalService usuarioPersonalService;

    public MetaComercialService(MetaComercialRepository metas, AlunoRepository alunos,
                                RegistroTreinoAlunoRepository registros, InteracaoCrmRepository interacoes,
                                UsuarioPersonalService usuarioPersonalService) {
        this.metas = metas; this.alunos = alunos; this.registros = registros;
        this.interacoes = interacoes; this.usuarioPersonalService = usuarioPersonalService;
    }

    @Transactional(readOnly = true)
    public MetaComercialDashboardView dashboard(Long personalId) {
        LocalDate mes = LocalDate.now().withDayOfMonth(1);
        MetaComercial meta = metas.findByPersonalIdAndMesReferencia(personalId, mes).orElseGet(() -> padrao(mes));
        long ativos = alunos.countByPersonalIdAndStatus(personalId, "ATIVO");
        long treinos = registros.countTreinosConcluidosNoPeriodo(personalId, mes, mes.plusMonths(1));
        long recuperacoes = interacoes.buscarRetencoesDesde(personalId, mes.atStartOfDay()).stream()
                .filter(i -> i.getDataContato().isBefore(mes.plusMonths(1).atStartOfDay()))
                .filter(i -> i.getResultado() == ResultadoCrm.RECUPERADO || i.getResultado() == ResultadoCrm.RENOVADO)
                .map(i -> i.getAluno().getId()).distinct().count();
        int pa = progresso(ativos, meta.getMetaAlunosAtivos());
        int pt = progresso(treinos, meta.getMetaTreinos());
        int pr = progresso(recuperacoes, meta.getMetaRecuperacoes());
        int geral = Math.round((pa + pt + pr) / 3f);
        String nivel = geral >= 100 ? "META BATIDA" : geral >= 75 ? "ACELERANDO" : geral >= 50 ? "NO CAMINHO" : "PRECISA DE FOCO";
        String mensagem = geral >= 100 ? "Excelente! Você atingiu todas as metas principais do mês." :
                geral >= 75 ? "Ótimo ritmo. Concentre esforço no indicador mais distante da meta." :
                "Use o painel para priorizar alunos, treinos e recuperação nesta etapa do mês.";
        return new MetaComercialDashboardView(label(mes), meta.getMetaAlunosAtivos(), ativos,
                meta.getMetaTreinos(), treinos, meta.getMetaRecuperacoes(), recuperacoes,
                pa, pt, pr, geral, nivel, mensagem);
    }

    @Transactional
    public void salvar(Long personalId, int alunosAtivos, int treinos, int recuperacoes) {
        if (alunosAtivos < 1 || alunosAtivos > 100000) throw new IllegalArgumentException("A meta de alunos ativos deve ser maior que zero.");
        if (treinos < 1 || treinos > 100000) throw new IllegalArgumentException("A meta de treinos deve ser maior que zero.");
        if (recuperacoes < 0 || recuperacoes > 100000) throw new IllegalArgumentException("A meta de recuperações deve ser igual ou maior que zero.");
        UsuarioPersonal personal = usuarioPersonalService.buscarPorId(personalId);
        LocalDate mes = LocalDate.now().withDayOfMonth(1);
        MetaComercial meta = metas.findByPersonalIdAndMesReferencia(personalId, mes).orElseGet(() -> {
            MetaComercial m = new MetaComercial(); m.setPersonal(personal); m.setMesReferencia(mes); return m;
        });
        meta.setMetaAlunosAtivos(alunosAtivos); meta.setMetaTreinos(treinos); meta.setMetaRecuperacoes(recuperacoes); metas.save(meta);
    }

    private MetaComercial padrao(LocalDate mes) { MetaComercial m = new MetaComercial(); m.setMesReferencia(mes); return m; }
    private int progresso(long atual, int meta) { return meta <= 0 ? 100 : Math.min(100, (int)Math.round(atual * 100D / meta)); }
    private String label(LocalDate mes) { return mes.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR")) + "/" + mes.getYear(); }
}
