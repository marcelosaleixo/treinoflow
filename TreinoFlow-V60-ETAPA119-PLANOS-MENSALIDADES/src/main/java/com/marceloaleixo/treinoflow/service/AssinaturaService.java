package com.marceloaleixo.treinoflow.service;
import com.marceloaleixo.treinoflow.entity.*; import com.marceloaleixo.treinoflow.enums.StatusAssinatura; import com.marceloaleixo.treinoflow.repository.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.LocalDate; import java.util.*;
@Service public class AssinaturaService{
 private final AssinaturaRepository repo; private final UsuarioPersonalRepository pessoais; private final PlanoRepository planos;
 public AssinaturaService(AssinaturaRepository repo,UsuarioPersonalRepository pessoais,PlanoRepository planos){this.repo=repo;this.pessoais=pessoais;this.planos=planos;}
 @Transactional public Assinatura salvar(Long id,Long personalId,Long planoId,StatusAssinatura status,LocalDate inicio,LocalDate vencimento,LocalDate proxima,LocalDate cancelamento,String obs){
  UsuarioPersonal p=pessoais.findById(personalId).orElseThrow(); Plano plano=planos.findById(planoId).orElseThrow(); Assinatura a=id==null?repo.findByPersonalId(personalId).orElseGet(Assinatura::new):repo.findById(id).orElseThrow();
  a.setPersonal(p); a.setPlano(plano); a.setStatus(status); a.setDataInicio(inicio); a.setDataVencimento(vencimento); a.setDataProximaCobranca(proxima); a.setDataCancelamento(cancelamento); a.setObservacao(obs); p.setPlano(plano); pessoais.save(p); return repo.save(a);
 }
 @Transactional public void atualizarVencidas(){for(Assinatura a:repo.findAll()){if(a.estaVencida()){a.setStatus(StatusAssinatura.VENCIDA);repo.save(a);}}}
 @Transactional public void atualizarVencida(Long personalId){repo.findByPersonalId(personalId).ifPresent(a->{if(a.estaVencida()){a.setStatus(StatusAssinatura.VENCIDA);repo.save(a);}});}
 public boolean podeAcessar(Long personalId){return repo.findByPersonalId(personalId).map(a->!a.estaVencida()&&a.permiteAcesso()).orElse(false);}
 public Optional<Assinatura> buscarPorPersonal(Long id){return repo.findByPersonalId(id);}
}
