package com.marceloaleixo.treinoflow.service;
import com.marceloaleixo.treinoflow.entity.*; import com.marceloaleixo.treinoflow.repository.*; import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.List;
@Service @Transactional public class TreinoExercicioService {
 @Autowired private TreinoExercicioRepository itens;
 @Autowired private TreinoRepository treinos;
 @Autowired private ExercicioRepository exercicios;
 @Transactional(readOnly=true) public List<TreinoExercicio> listar(Long treinoId,Long personalId){validarTreino(treinoId,personalId);return itens.listarComExercicioPorTreino(treinoId);}
 public TreinoExercicio adicionar(Long treinoId,Long personalId,Long exercicioId,TreinoExercicio item){Treino t=validarTreino(treinoId,personalId);if("LIBERADO".equals(t.getStatus()))throw new IllegalStateException("Não é possível alterar um treino liberado."); Exercicio e=exercicios.findById(exercicioId).orElseThrow(()->new IllegalArgumentException("Exercício não encontrado."));if(e.getPersonal()!=null&&!e.getPersonal().getId().equals(personalId))throw new IllegalArgumentException("Exercício não pertence a este personal.");if(item.getOrdem()==null||item.getOrdem()<1)throw new IllegalArgumentException("A ordem deve ser maior que zero.");if(item.getSeries()!=null&&item.getSeries()<1)throw new IllegalArgumentException("Séries deve ser maior que zero.");if(item.getDescansoSegundos()!=null&&item.getDescansoSegundos()<0)throw new IllegalArgumentException("Descanso não pode ser negativo.");item.setTreino(t);item.setExercicio(e);return itens.save(item);}
 public void mover(Long itemId, Long treinoId, Long personalId, int direcao){
  Treino treino=validarTreino(treinoId,personalId);
  if("LIBERADO".equals(treino.getStatus())) throw new IllegalStateException("Revogue o acesso antes de reorganizar um treino liberado.");
  if(direcao != -1 && direcao != 1) throw new IllegalArgumentException("Movimento inválido.");
  List<TreinoExercicio> lista=itens.findByTreinoIdOrderByOrdemAsc(treinoId);
  int indice=-1;
  for(int i=0;i<lista.size();i++) if(lista.get(i).getId().equals(itemId)){indice=i;break;}
  if(indice<0) throw new IllegalArgumentException("Exercício não pertence ao treino informado.");
  int destino=indice+direcao;
  if(destino<0 || destino>=lista.size()) return;
  // Renumera em duas fases para respeitar a constraint única (treino_id, ordem).
  for(int i=0;i<lista.size();i++){lista.get(i).setOrdem(-(i+1));itens.saveAndFlush(lista.get(i));}
  java.util.Collections.swap(lista,indice,destino);
  for(int i=0;i<lista.size();i++){lista.get(i).setOrdem(i+1);itens.saveAndFlush(lista.get(i));}
 }
 public TreinoExercicio atualizarPrescricao(Long itemId, Long treinoId, Long personalId, TreinoExercicio dados){
  Treino t=validarTreino(treinoId,personalId);
  if("LIBERADO".equals(t.getStatus())) throw new IllegalStateException("Revogue o acesso antes de alterar um treino liberado.");
  TreinoExercicio item=itens.findById(itemId).orElseThrow(()->new IllegalArgumentException("Exercício do treino não encontrado."));
  if(item.getTreino()==null || !item.getTreino().getId().equals(treinoId)) throw new IllegalArgumentException("Exercício não pertence ao treino informado.");
  if(dados.getSeries()!=null && dados.getSeries()<1) throw new IllegalArgumentException("Séries deve ser maior que zero.");
  if(dados.getDescansoSegundos()!=null && dados.getDescansoSegundos()<0) throw new IllegalArgumentException("Descanso não pode ser negativo.");
  item.setSeries(dados.getSeries());
  item.setRepeticoes(limpar(dados.getRepeticoes()));
  item.setCarga(limpar(dados.getCarga()));
  item.setDescansoSegundos(dados.getDescansoSegundos());
  item.setObservacao(limpar(dados.getObservacao()));
  return itens.save(item);
 }
 private String limpar(String valor){ return valor==null || valor.isBlank() ? null : valor.trim(); }
 public void excluir(Long itemId,Long treinoId,Long personalId){Treino t=validarTreino(treinoId,personalId);if("LIBERADO".equals(t.getStatus()))throw new IllegalStateException("Não é possível alterar um treino liberado.");TreinoExercicio item=itens.findById(itemId).orElseThrow(()->new IllegalArgumentException("Item não encontrado."));if(!item.getTreino().getId().equals(treinoId))throw new IllegalArgumentException("Item não pertence ao treino informado.");itens.delete(item);}
 private Treino validarTreino(Long id,Long personalId){return treinos.findByIdAndAlunoPersonalId(id,personalId).orElseThrow(()->new IllegalArgumentException("Treino não encontrado para este personal."));}
}
