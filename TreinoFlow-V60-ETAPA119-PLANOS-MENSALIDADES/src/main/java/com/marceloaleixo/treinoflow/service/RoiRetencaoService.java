package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.RoiRetencaoDashboardView;
import com.marceloaleixo.treinoflow.entity.AcaoAssistente;
import com.marceloaleixo.treinoflow.entity.AutomacaoRetencaoConfig;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.repository.AcaoAssistenteRepository;
import com.marceloaleixo.treinoflow.repository.AutomacaoRetencaoConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;

@Service
public class RoiRetencaoService {
    private final AcaoAssistenteRepository acoes;
    private final AutomacaoRetencaoConfigRepository configs;
    public RoiRetencaoService(AcaoAssistenteRepository acoes, AutomacaoRetencaoConfigRepository configs){this.acoes=acoes;this.configs=configs;}

    @Transactional(readOnly=true)
    public RoiRetencaoDashboardView dashboard(Long personalId, int dias){
        int periodo = dias==90 ? 90 : dias==365 ? 365 : 30;
        LocalDateTime fim=LocalDateTime.now(), inicio=fim.minusDays(periodo);
        List<AcaoAssistente> auto=acoes.buscarPeriodo(personalId,inicio,fim,true);
        List<AcaoAssistente> manual=acoes.buscarPeriodo(personalId,inicio,fim,false);
        long contatos=auto.size();
        long respostas=auto.stream().filter(a->finalizado(a.getResultado())).count();
        long recuperados=auto.stream().filter(a->a.getResultado()==ResultadoCrm.RECUPERADO).count();
        long renovados=auto.stream().filter(a->a.getResultado()==ResultadoCrm.RENOVADO).count();
        long cancelamentos=auto.stream().filter(a->a.getResultado()==ResultadoCrm.CANCELAMENTO).count();
        long base=recuperados+renovados+cancelamentos;
        double taxa=base==0?0:(recuperados+renovados)*100.0/base;
        AutomacaoRetencaoConfig c=configs.findByPersonalId(personalId).orElse(null);
        BigDecimal valor=c==null?BigDecimal.ZERO:c.getValorMensalAlunoEstimado();
        BigDecimal custoMensal=c==null?BigDecimal.ZERO:c.getCustoAutomacaoMensal();
        BigDecimal valorRec=valor.multiply(BigDecimal.valueOf(recuperados+renovados));
        BigDecimal custo=custoMensal.multiply(BigDecimal.valueOf(periodo/30.0)).setScale(2,RoundingMode.HALF_UP);
        BigDecimal resultado=valorRec.subtract(custo);
        Double roi=custo.signum()>0?resultado.multiply(BigDecimal.valueOf(100)).divide(custo,2,RoundingMode.HALF_UP).doubleValue():null;
        String melhor=melhorCanal(auto);
        List<RoiRetencaoDashboardView.FaixaRiscoView> faixas=List.of(
            faixa("Crítico",auto,75,100), faixa("Alto",auto,50,74), faixa("Médio",auto,25,49));
        return new RoiRetencaoDashboardView("Últimos "+periodo+" dias",auto.size(),manual.size(),contatos,respostas,recuperados,renovados,cancelamentos,taxa,valorRec,custo,resultado,roi,melhor,faixas);
    }
    private boolean finalizado(ResultadoCrm r){return r==ResultadoCrm.RECUPERADO||r==ResultadoCrm.RENOVADO||r==ResultadoCrm.CANCELAMENTO||r==ResultadoCrm.SEM_RESPOSTA;}
    private String melhorCanal(List<AcaoAssistente> a){
        Map<String,long[]> m=new HashMap<>();
        for(AcaoAssistente x:a){String k=x.getTipoAcao()==null?"Outro":x.getTipoAcao().getDescricao();long[] v=m.computeIfAbsent(k,k2->new long[2]);v[0]++;if(x.getResultado()==ResultadoCrm.RECUPERADO||x.getResultado()==ResultadoCrm.RENOVADO)v[1]++;}
        return m.entrySet().stream().filter(e->e.getValue()[0]>0).max(Comparator.comparingDouble(e->e.getValue()[1]*100.0/e.getValue()[0])).map(Map.Entry::getKey).orElse("Sem dados");
    }
    private RoiRetencaoDashboardView.FaixaRiscoView faixa(String nome,List<AcaoAssistente> a,int min,int max){List<AcaoAssistente>x=a.stream().filter(v->v.getScoreRisco()>=min&&v.getScoreRisco()<=max).toList();long rec=x.stream().filter(v->v.getResultado()==ResultadoCrm.RECUPERADO||v.getResultado()==ResultadoCrm.RENOVADO).count();long fin=x.stream().filter(v->v.getResultado()==ResultadoCrm.RECUPERADO||v.getResultado()==ResultadoCrm.RENOVADO||v.getResultado()==ResultadoCrm.CANCELAMENTO).count();return new RoiRetencaoDashboardView.FaixaRiscoView(nome,x.size(),rec,fin==0?0:rec*100.0/fin);}
}
