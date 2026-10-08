package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.ModeloTreino;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.AlunoService;
import com.marceloaleixo.treinoflow.service.ExercicioService;
import com.marceloaleixo.treinoflow.service.ModeloTreinoService;
import com.marceloaleixo.treinoflow.service.TreinoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ModeloTreinoController {
    @Autowired private ModeloTreinoService service;
    @Autowired private TreinoService treinoService;
    @Autowired private AlunoService alunoService;
    @Autowired private ExercicioService exercicioService;
    @Autowired private UsuarioPersonalService usuarioPersonalService;

    @GetMapping("/modelos-treino")
    public String listar(Authentication auth, Model model, @RequestParam(defaultValue="0") int page,
                         @RequestParam(defaultValue="10") int size, @RequestParam(defaultValue="") String q){
        Long pid=personal(auth).getId(); int pg=Math.max(0,page), sz=Math.min(50,Math.max(5,size));
        model.addAttribute("pagina",service.listar(pid,q,PageRequest.of(pg,sz)));
        model.addAttribute("q",q==null?"":q.trim());
        return "modelo-treino/lista";
    }

    @GetMapping("/modelos-treino/novo")
    public String novo(Model model){ model.addAttribute("modelo",new ModeloTreino()); return "modelo-treino/novo"; }

    @PostMapping("/modelos-treino/novo")
    public String criar(@ModelAttribute ModeloTreino modelo, Authentication auth, RedirectAttributes flash){
        try { ModeloTreino criado=service.criarVazio(personal(auth).getId(),modelo.getNome(),modelo.getDescricao());
            flash.addFlashAttribute("sucesso","Modelo criado. Agora adicione os exercícios e configure a prescrição.");
            return "redirect:/modelos-treino/"+criado.getId()+"/editar";
        } catch(IllegalArgumentException ex){ flash.addFlashAttribute("erro",ex.getMessage()); return "redirect:/modelos-treino/novo"; }
    }

    @GetMapping("/modelos-treino/{id}/editar")
    public String editar(@PathVariable Long id, Authentication auth, Model model){
        Long pid=personal(auth).getId();
        model.addAttribute("modelo",service.buscar(id,pid));
        model.addAttribute("itens",service.listarItens(id,pid));
        model.addAttribute("exercicios",exercicioService.listarDisponiveis(pid));
        return "modelo-treino/editor";
    }

    @PostMapping("/modelos-treino/{id}/dados")
    public String atualizarDados(@PathVariable Long id,@RequestParam String nome,@RequestParam(required=false) String descricao,
                                 Authentication auth,RedirectAttributes flash){
        try { service.atualizarDados(id,personal(auth).getId(),nome,descricao); flash.addFlashAttribute("sucesso","Dados do modelo atualizados."); }
        catch(IllegalArgumentException ex){flash.addFlashAttribute("erro",ex.getMessage());}
        return "redirect:/modelos-treino/"+id+"/editar";
    }

    @PostMapping("/modelos-treino/{id}/exercicios")
    public String adicionar(@PathVariable Long id,@RequestParam Long exercicioId,@RequestParam(required=false) Integer series,
                             @RequestParam(required=false) String repeticoes,@RequestParam(required=false) String carga,
                             @RequestParam(required=false) Integer descansoSegundos,@RequestParam(required=false) String observacao,
                             Authentication auth,RedirectAttributes flash){
        try {service.adicionarExercicio(id,exercicioId,personal(auth).getId(),series,repeticoes,carga,descansoSegundos,observacao);flash.addFlashAttribute("sucesso","Exercício adicionado ao modelo.");}
        catch(IllegalArgumentException ex){flash.addFlashAttribute("erro",ex.getMessage());}
        return "redirect:/modelos-treino/"+id+"/editar";
    }

    @PostMapping("/modelos-treino/{id}/exercicios/{itemId}/atualizar")
    public String atualizarItem(@PathVariable Long id,@PathVariable Long itemId,@RequestParam(required=false) Integer series,
                                @RequestParam(required=false) String repeticoes,@RequestParam(required=false) String carga,
                                @RequestParam(required=false) Integer descansoSegundos,@RequestParam(required=false) String observacao,
                                Authentication auth,RedirectAttributes flash){
        try {service.atualizarExercicio(id,itemId,personal(auth).getId(),series,repeticoes,carga,descansoSegundos,observacao);flash.addFlashAttribute("sucesso","Prescrição atualizada.");}
        catch(IllegalArgumentException ex){flash.addFlashAttribute("erro",ex.getMessage());}
        return "redirect:/modelos-treino/"+id+"/editar";
    }

    @PostMapping("/modelos-treino/{id}/exercicios/{itemId}/mover")
    public String mover(@PathVariable Long id,@PathVariable Long itemId,@RequestParam int direcao,Authentication auth,RedirectAttributes flash){
        try {service.moverExercicio(id,itemId,personal(auth).getId(),direcao);}
        catch(IllegalArgumentException ex){flash.addFlashAttribute("erro",ex.getMessage());}
        return "redirect:/modelos-treino/"+id+"/editar";
    }

    @PostMapping("/modelos-treino/{id}/exercicios/{itemId}/excluir")
    public String remover(@PathVariable Long id,@PathVariable Long itemId,Authentication auth,RedirectAttributes flash){
        try {service.removerExercicio(id,itemId,personal(auth).getId());flash.addFlashAttribute("sucesso","Exercício removido do modelo.");}
        catch(IllegalArgumentException ex){flash.addFlashAttribute("erro",ex.getMessage());}
        return "redirect:/modelos-treino/"+id+"/editar";
    }

    @GetMapping("/treinos/{treinoId}/salvar-como-modelo")
    public String salvarForm(@PathVariable Long treinoId, Authentication auth, Model model){
        Long pid=personal(auth).getId(); model.addAttribute("treino",treinoService.buscarPorIdComAluno(treinoId,pid)); model.addAttribute("modelo",new ModeloTreino()); return "modelo-treino/form";
    }

    @PostMapping("/treinos/{treinoId}/salvar-como-modelo")
    public String salvar(@PathVariable Long treinoId,@ModelAttribute ModeloTreino modelo,Authentication auth,RedirectAttributes flash){
        try{service.criarAPartirDeTreino(treinoId,personal(auth).getId(),modelo.getNome(),modelo.getDescricao());flash.addFlashAttribute("sucesso","Modelo criado com sucesso.");return "redirect:/modelos-treino";}
        catch(IllegalArgumentException ex){flash.addFlashAttribute("erro",ex.getMessage());return "redirect:/treinos/"+treinoId+"/salvar-como-modelo";}
    }

    @GetMapping("/modelos-treino/{id}/aplicar")
    public String aplicarForm(@PathVariable Long id,Authentication auth,Model model){Long pid=personal(auth).getId();model.addAttribute("modelo",service.buscar(id,pid));model.addAttribute("itens",service.listarItens(id,pid));model.addAttribute("alunos",alunoService.listarAtivosPorPersonal(pid));return "modelo-treino/aplicar";}

    @PostMapping("/modelos-treino/{id}/aplicar")
    public String aplicar(@PathVariable Long id,@RequestParam Long alunoId,Authentication auth,RedirectAttributes flash){try{var treino=service.aplicar(id,alunoId,personal(auth).getId());flash.addFlashAttribute("sucesso","Modelo aplicado. Revise a prescrição antes de liberar.");return "redirect:/treinos/"+treino.getId()+"/editar";}catch(IllegalArgumentException ex){flash.addFlashAttribute("erro",ex.getMessage());return "redirect:/modelos-treino/"+id+"/aplicar";}}

    @PostMapping("/modelos-treino/{id}/excluir")
    public String excluir(@PathVariable Long id,Authentication auth,RedirectAttributes flash){try{service.excluir(id,personal(auth).getId());flash.addFlashAttribute("sucesso","Modelo excluído.");}catch(IllegalArgumentException ex){flash.addFlashAttribute("erro",ex.getMessage());}return "redirect:/modelos-treino";}

    private UsuarioPersonal personal(Authentication auth){return usuarioPersonalService.buscarPorEmail(auth.getName());}
}
