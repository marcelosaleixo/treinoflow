package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.Exercicio;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.ExercicioService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ExercicioController {
    @Autowired private ExercicioService exercicioService;
    @Autowired private UsuarioPersonalService usuarioPersonalService;

    @GetMapping("/exercicios")
    public String listar(Authentication auth, Model model,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "5") int size,
                         @RequestParam(required = false) String q) {
        int paginaAtual = Math.max(0, page);
        int tamanhoPagina = Math.min(50, Math.max(5, size));
        model.addAttribute("pagina", exercicioService.listarPaginado(personal(auth).getId(), q, PageRequest.of(paginaAtual, tamanhoPagina)));
        model.addAttribute("q", q == null ? "" : q.trim());
        return "exercicio/lista";
    }
    @GetMapping("/exercicios/novo")
    public String novo(Authentication auth, Model model) {
        model.addAttribute("exercicio", new Exercicio());
        carregarGruposMusculares(auth, model);
        return "exercicio/form";
    }
    @GetMapping("/exercicios/{id}/editar")
    public String editar(@PathVariable Long id, Authentication auth, Model model) {
        Exercicio e = exercicioService.buscarDoPersonal(id, personal(auth).getId());
        model.addAttribute("exercicio", e);
        carregarGruposMusculares(auth, model);
        return "exercicio/form";
    }
    @PostMapping("/exercicios/salvar")
    public String salvar(@ModelAttribute Exercicio exercicio, Authentication auth, RedirectAttributes flash) {
        try { exercicioService.salvar(personal(auth).getId(), exercicio); flash.addFlashAttribute("sucesso", "Exercício salvo."); return "redirect:/exercicios"; }
        catch (IllegalArgumentException ex) { flash.addFlashAttribute("erro", ex.getMessage()); return "redirect:/exercicios/novo"; }
    }
    @PostMapping("/exercicios/{id}/excluir")
    public String excluir(@PathVariable Long id, Authentication auth, RedirectAttributes flash) {
        try { exercicioService.excluir(id, personal(auth).getId()); flash.addFlashAttribute("sucesso", "Exercício excluído."); }
        catch (IllegalArgumentException ex) { flash.addFlashAttribute("erro", ex.getMessage()); }
        return "redirect:/exercicios";
    }
    private void carregarGruposMusculares(Authentication auth, Model model) {
        model.addAttribute(
                "gruposMusculares",
                exercicioService.listarGruposMusculares(personal(auth).getId())
        );
    }

    private UsuarioPersonal personal(Authentication auth) {
        return usuarioPersonalService.buscarPorEmail(auth.getName());
    }
}
