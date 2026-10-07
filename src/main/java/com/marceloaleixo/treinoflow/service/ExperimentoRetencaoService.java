package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.ExperimentoRetencao;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente;
import com.marceloaleixo.treinoflow.repository.ExperimentoRetencaoRepository;
import com.marceloaleixo.treinoflow.repository.AcaoAssistenteRepository;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

/** Etapa 81: A/B testing controlado de mensagens de retenção. */
@Service
public class ExperimentoRetencaoService {
    private final ExperimentoRetencaoRepository repository;
    private final UsuarioPersonalRepository personais;
    private final AprendizadoRetencaoService aprendizado;
    private final AcaoAssistenteRepository acoes;
    public ExperimentoRetencaoService(ExperimentoRetencaoRepository repository, UsuarioPersonalRepository personais, AprendizadoRetencaoService aprendizado, AcaoAssistenteRepository acoes){
        this.repository=repository; this.personais=personais; this.aprendizado=aprendizado; this.acoes=acoes;
    }

    @Transactional
    public Escolha escolher(Long personalId, Long alunoId, int score, TipoAcaoAssistente tipo){
        String faixa=faixa(score);
        List<AprendizadoRetencaoService.ModeloVencedor> modelos=aprendizado.modelosElegiveis(personalId, score, tipo);
        if(modelos.size()<2) return new Escolha(null,null,"SEM_EXPERIMENTO");
        ExperimentoRetencao exp=repository.findFirstByPersonalIdAndFaixaRiscoAndTipoAcaoAndStatusOrderByCriadoEmDesc(personalId,faixa,tipo,"ATIVO").orElseGet(() -> criar(personalId,faixa,tipo,modelos.get(0).modelo(),modelos.get(1).modelo()));
        String variante=((alunoId == null ? 0L : alunoId) ^ exp.getId()) % 2 == 0 ? "A" : "B";
        return new Escolha(exp.getId(), variante, "AB_TEST");
    }

    @Transactional(readOnly=true)
    public List<Resumo> listar(Long personalId){
        return repository.findByPersonalIdOrderByCriadoEmDesc(personalId).stream().map(e -> resumo(e, acoes.buscarPorExperimento(personalId, e.getId()))).toList();
    }

    @Transactional
    public void encerrar(Long personalId, Long id){
        ExperimentoRetencao exp=repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Experimento não encontrado."));
        if(exp.getPersonal()==null || !Objects.equals(exp.getPersonal().getId(),personalId)) throw new IllegalArgumentException("Experimento não pertence a este personal.");
        exp.setStatus("ENCERRADO"); exp.setEncerradoEm(LocalDateTime.now()); repository.save(exp);
    }

    public String mensagem(Long id, String variante){
        if(id==null || variante==null) return null;
        return repository.findById(id).map(e -> "A".equals(variante)?e.getVarianteA():e.getVarianteB()).orElse(null);
    }

    private ExperimentoRetencao criar(Long personalId,String faixa,TipoAcaoAssistente tipo,String a,String b){
        UsuarioPersonal p=personais.findById(personalId).orElseThrow();
        ExperimentoRetencao e=new ExperimentoRetencao(); e.setPersonal(p); e.setFaixaRisco(faixa); e.setTipoAcao(tipo); e.setVarianteA(a); e.setVarianteB(b); e.setStatus("ATIVO"); return repository.save(e);
    }
    private Resumo resumo(ExperimentoRetencao e, List<com.marceloaleixo.treinoflow.entity.AcaoAssistente> historico){
        var a=historico.stream().filter(x -> "A".equals(x.getExperimentoVariante())).toList();
        var b=historico.stream().filter(x -> "B".equals(x.getExperimentoVariante())).toList();
        return new Resumo(e.getId(),e.getFaixaRisco(),e.getTipoAcao().getDescricao(),e.getStatus(),e.getVarianteA(),e.getVarianteB(),e.getCriadoEm(),e.getEncerradoEm(),estat(a),estat(b));
    }
    private Estatistica estat(List<com.marceloaleixo.treinoflow.entity.AcaoAssistente> l){
        long sucesso=l.stream().filter(x -> x.getResultado()==ResultadoCrm.RECUPERADO || x.getResultado()==ResultadoCrm.RENOVADO).count();
        double taxa=l.isEmpty()?0:sucesso*100.0/l.size();
        return new Estatistica(l.size(),sucesso,taxa);
    }
    private String faixa(int score){if(score>=75)return "CRÍTICO"; if(score>=50)return "ALTO"; return "MÉDIO";}
    public record Escolha(Long experimentoId,String variante,String origem){}
    public record Resumo(Long id,String faixa,String tipo,String status,String varianteA,String varianteB,LocalDateTime criadoEm,LocalDateTime encerradoEm,Estatistica estatisticaA,Estatistica estatisticaB){}
    public record Estatistica(long amostras,long sucessos,double taxa){}
}
