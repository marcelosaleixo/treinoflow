package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.MetaRetencaoDashboardView;
import com.marceloaleixo.treinoflow.dto.MesMetaRetencaoView;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.MetaRetencao;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.repository.MetaRetencaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class MetaRetencaoService {
    private final MetaRetencaoRepository metas;
    private final InteracaoCrmRepository interacoes;
    private final UsuarioPersonalService usuarioPersonalService;

    public MetaRetencaoService(MetaRetencaoRepository metas, InteracaoCrmRepository interacoes,
                               UsuarioPersonalService usuarioPersonalService) {
        this.metas = metas;
        this.interacoes = interacoes;
        this.usuarioPersonalService = usuarioPersonalService;
    }

    @Transactional(readOnly = true)
    public MetaRetencaoDashboardView dashboard(Long personalId) {
        LocalDate atual = LocalDate.now().withDayOfMonth(1);
        MetaRetencao meta = obterOuPadrao(personalId, atual);
        MesDados dados = calcular(personalId, atual, meta);
        List<MesMetaRetencaoView> historico = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            LocalDate mes = atual.minusMonths(i);
            MetaRetencao m = metas.findByPersonalIdAndMesReferencia(personalId, mes).orElse(metaPadrao(mes));
            MesDados d = calcular(personalId, mes, m);
            historico.add(new MesMetaRetencaoView(label(mes), m.getMetaRecuperacoes(), d.recuperados, m.getMetaTaxa(), d.taxa, atingimento(d.recuperados, m.getMetaRecuperacoes())));
        }
        return new MetaRetencaoDashboardView(label(atual), meta.getMetaRecuperacoes(), dados.recuperados, meta.getMetaTaxa(), dados.taxa,
                atingimento(dados.recuperados, meta.getMetaRecuperacoes()), percentualTaxa(dados.taxa, meta.getMetaTaxa()), historico);
    }

    @Transactional
    public void salvarMeta(Long personalId, int metaRecuperacoes, double metaTaxa) {
        if (metaRecuperacoes < 1 || metaRecuperacoes > 10000) throw new IllegalArgumentException("A meta de recuperações deve ser maior que zero.");
        if (metaTaxa < 0 || metaTaxa > 100) throw new IllegalArgumentException("A meta de taxa deve estar entre 0 e 100%.");
        UsuarioPersonal personal = usuarioPersonalService.buscarPorId(personalId);
        LocalDate mes = LocalDate.now().withDayOfMonth(1);
        MetaRetencao meta = metas.findByPersonalIdAndMesReferencia(personalId, mes).orElseGet(() -> {
            MetaRetencao nova = new MetaRetencao(); nova.setPersonal(personal); nova.setMesReferencia(mes); return nova;
        });
        meta.setMetaRecuperacoes(metaRecuperacoes);
        meta.setMetaTaxa(metaTaxa);
        metas.save(meta);
    }

    private MesDados calcular(Long personalId, LocalDate mes, MetaRetencao meta) {
        LocalDateTime inicio = mes.atStartOfDay();
        LocalDateTime fim = mes.plusMonths(1).atStartOfDay();
        List<InteracaoCrm> lista = interacoes.buscarRetencoesDesde(personalId, inicio).stream()
                .filter(i -> i.getDataContato().isBefore(fim)).toList();
        long recuperados = lista.stream()
                .filter(i -> i.getResultado() == ResultadoCrm.RECUPERADO || i.getResultado() == ResultadoCrm.RENOVADO)
                .map(i -> i.getAluno().getId()).distinct().count();
        long cancelamentos = lista.stream().filter(i -> i.getResultado() == ResultadoCrm.CANCELAMENTO)
                .map(i -> i.getAluno().getId()).distinct().count();
        double taxa = recuperados + cancelamentos == 0 ? 0D : recuperados * 100D / (recuperados + cancelamentos);
        return new MesDados(recuperados, taxa);
    }

    private MetaRetencao obterOuPadrao(Long personalId, LocalDate mes) {
        return metas.findByPersonalIdAndMesReferencia(personalId, mes).orElseGet(() -> metaPadrao(mes));
    }
    private MetaRetencao metaPadrao(LocalDate mes) {
        MetaRetencao m = new MetaRetencao(); m.setMesReferencia(mes); m.setMetaRecuperacoes(5); m.setMetaTaxa(60D); return m;
    }
    private int atingimento(long atual, int meta) { return meta <= 0 ? 0 : Math.min(100, (int)Math.round(atual * 100D / meta)); }
    private int percentualTaxa(double atual, double meta) { return meta <= 0 ? 100 : Math.min(100, (int)Math.round(atual * 100D / meta)); }
    private String label(LocalDate mes) { return mes.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR")) + "/" + mes.format(DateTimeFormatter.ofPattern("yy")); }
    private record MesDados(long recuperados, double taxa) {}
}
