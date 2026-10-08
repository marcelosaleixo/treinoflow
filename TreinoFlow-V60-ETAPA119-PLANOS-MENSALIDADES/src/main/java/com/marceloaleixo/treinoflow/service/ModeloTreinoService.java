package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.*;
import com.marceloaleixo.treinoflow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ModeloTreinoService {
    @Autowired private ModeloTreinoRepository modelos;
    @Autowired private ModeloTreinoExercicioRepository itens;
    @Autowired private TreinoRepository treinos;
    @Autowired private TreinoExercicioRepository itensTreino;
    @Autowired private ExercicioRepository exercicios;
    @Autowired private AlunoRepository alunos;
    @Autowired private UsuarioPersonalRepository personais;

    @Transactional(readOnly=true)
    public Page<ModeloTreino> listar(Long personalId, String q, Pageable pageable){
        if(q==null || q.isBlank()) return modelos.findByPersonalIdOrderByNomeAsc(personalId,pageable);
        return modelos.findByPersonalIdAndNomeContainingIgnoreCaseOrderByNomeAsc(personalId,q.trim(),pageable);
    }

    @Transactional(readOnly=true)
    public ModeloTreino buscar(Long id, Long personalId){
        return modelos.findByIdAndPersonalId(id,personalId)
                .orElseThrow(()->new IllegalArgumentException("Modelo não encontrado."));
    }

    @Transactional(readOnly=true)
    public List<ModeloTreinoExercicio> listarItens(Long id, Long personalId){
        buscar(id,personalId);
        return itens.listar(id);
    }

    public ModeloTreino criarVazio(Long personalId, String nome, String descricao){
        validarNome(nome, personalId, null);
        ModeloTreino m=new ModeloTreino();
        m.setPersonal(personais.findById(personalId).orElseThrow(() -> new IllegalArgumentException("Personal não encontrado.")));
        m.setNome(nome.trim());
        m.setDescricao(normalizarDescricao(descricao));
        return modelos.save(m);
    }

    public ModeloTreino atualizarDados(Long id, Long personalId, String nome, String descricao){
        ModeloTreino m=buscar(id,personalId);
        validarNome(nome, personalId, id);
        m.setNome(nome.trim());
        m.setDescricao(normalizarDescricao(descricao));
        return modelos.save(m);
    }

    public void criarAPartirDeTreino(Long treinoId, Long personalId, String nome, String descricao){
        Treino origem=treinos.findByIdAndAlunoPersonalId(treinoId,personalId)
                .orElseThrow(()->new IllegalArgumentException("Treino não encontrado."));
        validarNome(nome, personalId, null);
        ModeloTreino m=new ModeloTreino();
        m.setPersonal(personais.findById(personalId).orElseThrow());
        m.setNome(nome.trim());
        m.setDescricao(descricao==null||descricao.isBlank()?origem.getDescricao():descricao.trim());
        m=modelos.save(m);
        for(TreinoExercicio o:itensTreino.findByTreinoIdOrderByOrdemAsc(treinoId)){
            ModeloTreinoExercicio n=new ModeloTreinoExercicio();
            n.setModelo(m); n.setExercicio(o.getExercicio()); n.setOrdem(o.getOrdem());
            n.setSeries(o.getSeries()); n.setRepeticoes(o.getRepeticoes()); n.setCarga(o.getCarga());
            n.setDescansoSegundos(o.getDescansoSegundos()); n.setObservacao(o.getObservacao());
            itens.save(n);
        }
    }

    public void adicionarExercicio(Long modeloId, Long exercicioId, Long personalId, Integer series,
                                   String repeticoes, String carga, Integer descansoSegundos, String observacao){
        ModeloTreino m=buscar(modeloId,personalId);
        Exercicio ex=exercicios.findDisponivelParaPersonal(exercicioId,personalId)
                .orElseThrow(() -> new IllegalArgumentException("Exercício não encontrado ou indisponível para este personal."));
        if(itens.existsByModeloIdAndExercicioId(modeloId,exercicioId))
            throw new IllegalArgumentException("Este exercício já está no modelo.");
        int ordem=itens.proximaOrdem(modeloId);
        ModeloTreinoExercicio item=new ModeloTreinoExercicio();
        item.setModelo(m); item.setExercicio(ex); item.setOrdem(ordem); item.setSeries(series);
        item.setRepeticoes(limpar(repeticoes)); item.setCarga(limpar(carga)); item.setDescansoSegundos(descansoSegundos);
        item.setObservacao(limpar(observacao));
        itens.save(item);
    }

    public void atualizarExercicio(Long modeloId, Long itemId, Long personalId, Integer series,
                                   String repeticoes, String carga, Integer descansoSegundos, String observacao){
        buscar(modeloId,personalId);
        ModeloTreinoExercicio item=itens.findByIdAndModeloId(itemId,modeloId)
                .orElseThrow(() -> new IllegalArgumentException("Exercício do modelo não encontrado."));
        item.setSeries(series); item.setRepeticoes(limpar(repeticoes)); item.setCarga(limpar(carga));
        item.setDescansoSegundos(descansoSegundos); item.setObservacao(limpar(observacao));
        itens.save(item);
    }

    public void removerExercicio(Long modeloId, Long itemId, Long personalId){
        buscar(modeloId,personalId);
        ModeloTreinoExercicio item=itens.findByIdAndModeloId(itemId,modeloId)
                .orElseThrow(() -> new IllegalArgumentException("Exercício do modelo não encontrado."));
        itens.delete(item);
        reorganizarOrdens(modeloId);
    }

    public void moverExercicio(Long modeloId, Long itemId, Long personalId, int direcao){
        buscar(modeloId,personalId);
        if(direcao!=1 && direcao!=-1) throw new IllegalArgumentException("Direção inválida.");
        List<ModeloTreinoExercicio> lista=itens.listar(modeloId);
        int atual=-1;
        for(int i=0;i<lista.size();i++) if(lista.get(i).getId().equals(itemId)){atual=i;break;}
        if(atual<0) throw new IllegalArgumentException("Exercício do modelo não encontrado.");
        int destino=atual+direcao;
        if(destino<0 || destino>=lista.size()) return;
        ModeloTreinoExercicio a=lista.get(atual), b=lista.get(destino);
        // Evita conflito da constraint unique(modelo_id, ordem) durante a troca.
        a.setOrdem(100000 + atual);
        b.setOrdem(100000 + destino);
        itens.flush();
        a.setOrdem(destino+1);
        b.setOrdem(atual+1);
        itens.save(a); itens.save(b);
    }

    public Treino aplicar(Long modeloId, Long alunoId, Long personalId){
        ModeloTreino m=buscar(modeloId,personalId);
        Aluno aluno=alunos.findByIdAndPersonalId(alunoId,personalId)
                .orElseThrow(()->new IllegalArgumentException("Aluno não encontrado para este personal."));
        Treino t=new Treino(); t.setAluno(aluno); String nome=m.getNome();
        t.setNome(nome.length()>80?nome.substring(0,80):nome); t.setDescricao(m.getDescricao());
        t.setStatus("RASCUNHO"); t.setTotalVisualizacoes(0); t=treinos.save(t);
        for(ModeloTreinoExercicio o:itens.listar(modeloId)){
            TreinoExercicio n=new TreinoExercicio(); n.setTreino(t); n.setExercicio(o.getExercicio()); n.setOrdem(o.getOrdem());
            n.setSeries(o.getSeries()); n.setRepeticoes(o.getRepeticoes()); n.setCarga(o.getCarga());
            n.setDescansoSegundos(o.getDescansoSegundos()); n.setObservacao(o.getObservacao()); itensTreino.save(n);
        }
        return t;
    }

    public void excluir(Long id,Long personalId){
        ModeloTreino m=buscar(id,personalId); itens.deleteByModeloId(id); modelos.delete(m);
    }

    private void validarNome(String nome, Long personalId, Long ignorarId){
        if(nome==null||nome.isBlank()||nome.trim().length()>80)
            throw new IllegalArgumentException("Nome do modelo é obrigatório (até 80 caracteres).");
        boolean existe=modelos.existsByNomeIgnoreCaseAndPersonalId(nome.trim(),personalId);
        if(existe && (ignorarId==null || modelos.findByIdAndPersonalId(ignorarId,personalId).map(m->!m.getNome().equalsIgnoreCase(nome.trim())).orElse(true)))
            throw new IllegalArgumentException("Você já possui um modelo com esse nome.");
    }

    private String normalizarDescricao(String valor){return limpar(valor);}
    private String limpar(String valor){return valor==null||valor.isBlank()?null:valor.trim();}
    private void reorganizarOrdens(Long modeloId){
        List<ModeloTreinoExercicio> lista=itens.listar(modeloId);
        for(int i=0;i<lista.size();i++) lista.get(i).setOrdem(100000+i);
        itens.flush();
        for(int i=0;i<lista.size();i++){lista.get(i).setOrdem(i+1);itens.save(lista.get(i));}
    }
}
