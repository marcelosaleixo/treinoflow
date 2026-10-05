package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.Treino;
import com.marceloaleixo.treinoflow.service.EvolucaoAlunoService;
import com.marceloaleixo.treinoflow.service.PortalAlunoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/portal")
public class PortalAlunoController {
    private final PortalAlunoService portal;
    private final EvolucaoAlunoService evolucao;

    public PortalAlunoController(PortalAlunoService portal, EvolucaoAlunoService evolucao) {
        this.portal = portal;
        this.evolucao = evolucao;
    }

    @GetMapping("/{token}")
    public String inicio(@PathVariable String token, Model model) {
        try {
            Aluno aluno = portal.buscarAlunoPorToken(token);
            model.addAttribute("aluno", aluno);
            model.addAttribute("treinos", portal.listarTreinos(aluno));
            model.addAttribute("historico", portal.historico(aluno));
            model.addAttribute("evolucao", evolucao.montar(aluno));
            model.addAttribute("token", token);
            return "portal/index";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("erro", ex.getMessage());
            return "portal/indisponivel";
        }
    }

    @GetMapping("/{token}/treinos/{treinoId}")
    public String treino(@PathVariable String token, @PathVariable Long treinoId, Model model) {
        try {
            Aluno aluno = portal.buscarAlunoPorToken(token);
            Treino treino = portal.buscarTreino(aluno, treinoId);
            model.addAttribute("aluno", aluno);
            model.addAttribute("treino", treino);
            model.addAttribute("exercicios", portal.listarExercicios(treino));
            model.addAttribute("ultimaExecucao", portal.ultimaExecucao(treino));
            model.addAttribute("exerciciosConcluidos", portal.exerciciosConcluidosHoje(treino));
            model.addAttribute("token", token);
            return "portal/treino";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("erro", ex.getMessage());
            return "portal/indisponivel";
        }
    }

    @PostMapping("/{token}/treinos/{treinoId}/concluir")
    public String concluir(@PathVariable String token, @PathVariable Long treinoId,
                           @RequestParam(required = false) Integer nota,
                           @RequestParam(required = false) String feedback,
                           @RequestParam Map<String, String> parametros,
                           RedirectAttributes ra) {
        try {
            Aluno aluno = portal.buscarAlunoPorToken(token);
            portal.registrarConclusao(aluno, treinoId, nota, feedback, parametros);
            ra.addFlashAttribute("sucesso", "Progresso do treino salvo com sucesso.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/portal/" + token + "/treinos/" + treinoId;
    }

}
