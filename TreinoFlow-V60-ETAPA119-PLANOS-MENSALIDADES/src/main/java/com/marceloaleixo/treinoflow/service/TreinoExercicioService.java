package com.marceloaleixo.treinoflow.service;
import com.marceloaleixo.treinoflow.entity.*; import com.marceloaleixo.treinoflow.repository.*; import com.marceloaleixo.treinoflow.dto.PrescricaoAssistidaView; import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.List;
@Service @Transactional public class TreinoExercicioService {
 @Autowired private TreinoExercicioRepository itens;
 @Autowired private TreinoRepository treinos;
 @Autowired private ExercicioRepository exercicios;
 @Autowired private PrescricaoAssistidaService prescricaoAssistidaService;
 @Autowired private com.marceloaleixo.treinoflow.repository.PrescricaoAuditoriaRepository prescricaoAuditoriaRepository;
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
 public TreinoExercicio aplicarPrescricaoAssistida(Long itemId, Long alunoId, Long personalId){
  TreinoExercicio item=itens.findById(itemId).orElseThrow(()->new IllegalArgumentException("Exercício do treino não encontrado."));
  if(item.getTreino()==null || item.getTreino().getAluno()==null || !item.getTreino().getAluno().getId().equals(alunoId))
   throw new IllegalArgumentException("Exercício não pertence ao aluno informado.");
  PrescricaoAssistidaView sugestao=prescricaoAssistidaService.sugerir(item, item.getTreino().getAluno());
  return aplicarPrescricaoAssistida(itemId, alunoId, personalId, sugestao);
 }

 public TreinoExercicio aplicarPrescricaoAssistida(Long itemId, Long alunoId, Long personalId, PrescricaoAssistidaView sugestao){
  if(sugestao == null) throw new IllegalArgumentException("Sugestão de prescrição não encontrada.");
  if(!"PROGRESSAO".equals(sugestao.nivel()) || !"ALTA".equals(sugestao.confianca()))
   throw new IllegalStateException("Apenas sugestões de progressão com alta confiança podem ser aplicadas diretamente.");
  TreinoExercicio item=itens.findById(itemId).orElseThrow(()->new IllegalArgumentException("Exercício do treino não encontrado."));
  if(item.getTreino()==null || item.getTreino().getAluno()==null || !item.getTreino().getAluno().getId().equals(alunoId))
   throw new IllegalArgumentException("Exercício não pertence ao aluno informado.");
  Treino treino=validarTreino(item.getTreino().getId(),personalId);
  if(!treino.getAluno().getId().equals(alunoId)) throw new IllegalArgumentException("Aluno não pertence a este Personal.");
  if("LIBERADO".equals(treino.getStatus())) throw new IllegalStateException("Revogue o acesso antes de aplicar uma nova prescrição.");
  Integer series=converterSeries(sugestao.seriesSugeridas());
  if(series == null || series < 1) throw new IllegalStateException("A sugestão não possui séries válidas para aplicação.");
  if(sugestao.repeticoesSugeridas()==null || sugestao.repeticoesSugeridas().isBlank()) throw new IllegalStateException("A sugestão não possui repetições válidas para aplicação.");
  if(sugestao.cargaSugerida()==null || sugestao.cargaSugerida().isBlank() || sugestao.cargaSugerida().contains("definir pelo Personal")) throw new IllegalStateException("A sugestão não possui carga objetiva para aplicação.");
  String seriesAntes = item.getSeries() == null ? null : item.getSeries().toString();
  String repeticoesAntes = item.getRepeticoes();
  String cargaAntes = item.getCarga();
  item.setSeries(series); item.setRepeticoes(limpar(sugestao.repeticoesSugeridas())); item.setCarga(limpar(sugestao.cargaSugerida()));
  TreinoExercicio salvo = itens.save(item);
  PrescricaoAuditoria auditoria = new PrescricaoAuditoria();
  auditoria.setPersonal(treino.getAluno().getPersonal());
  auditoria.setAluno(treino.getAluno());
  auditoria.setTreino(treino);
  auditoria.setTreinoExercicio(salvo);
  auditoria.setExercicioNome(salvo.getExercicio() == null ? "Exercício" : salvo.getExercicio().getNome());
  auditoria.setNivelIa(sugestao.nivel());
  auditoria.setConfiancaIa(sugestao.confianca());
  auditoria.setJustificativa(sugestao.justificativa());
  auditoria.setSeriesAntes(seriesAntes); auditoria.setRepeticoesAntes(repeticoesAntes); auditoria.setCargaAntes(cargaAntes);
  auditoria.setSeriesDepois(salvo.getSeries() == null ? null : salvo.getSeries().toString());
  auditoria.setRepeticoesDepois(salvo.getRepeticoes()); auditoria.setCargaDepois(salvo.getCarga());
  auditoria.setDecisao("APLICADA");
  prescricaoAuditoriaRepository.save(auditoria);
  return salvo;
 }
 private Integer converterSeries(String valor){ try { if(valor==null) return null; java.util.regex.Matcher m=java.util.regex.Pattern.compile("\\d+").matcher(valor); return m.find()?Integer.valueOf(m.group()):null; } catch(Exception e){ return null; } }
 public void excluir(Long itemId,Long treinoId,Long personalId){Treino t=validarTreino(treinoId,personalId);if("LIBERADO".equals(t.getStatus()))throw new IllegalStateException("Não é possível alterar um treino liberado.");TreinoExercicio item=itens.findById(itemId).orElseThrow(()->new IllegalArgumentException("Item não encontrado."));if(!item.getTreino().getId().equals(treinoId))throw new IllegalArgumentException("Item não pertence ao treino informado.");itens.delete(item);}
 private Treino validarTreino(Long id,Long personalId){return treinos.findByIdAndAlunoPersonalId(id,personalId).orElseThrow(()->new IllegalArgumentException("Treino não encontrado para este personal."));}
}
