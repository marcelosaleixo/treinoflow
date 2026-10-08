package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.service.ResultadoAcoesAssistenteService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ResultadoAcoesAssistenteController {
    private final ResultadoAcoesAssistenteService service;
    private final UsuarioPersonalService usuarioPersonalService;

    public ResultadoAcoesAssistenteController(ResultadoAcoesAssistenteService service, UsuarioPersonalService usuarioPersonalService) {
        this.service = service;
        this.usuarioPersonalService = usuarioPersonalService;
    }

    @GetMapping("/assistente/resultados")
    public String index(Authentication authentication, Model model,
                        @RequestParam(defaultValue = "30") int dias) {
        UsuarioPersonal personal = personal(authentication);
        model.addAttribute("personal", personal);
        model.addAttribute("dashboard", service.dashboard(personal.getId(), dias));
        model.addAttribute("dias", dias);
        model.addAttribute("resultados", ResultadoCrm.values());
        return "assistente/resultados";
    }

    @PostMapping("/assistente/resultados/{id}")
    public String resultado(@PathVariable Long id, @RequestParam ResultadoCrm resultado,
                            Authentication authentication, RedirectAttributes ra) {
        try {
            UsuarioPersonal personal = personal(authentication);
            service.registrarResultado(personal.getId(), id, resultado);
            ra.addFlashAttribute("sucesso", "Resultado da ação atualizado.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/assistente/resultados";
    }

    private UsuarioPersonal personal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) throw new IllegalArgumentException("Usuário não autenticado.");
        return usuarioPersonalService.buscarPorEmail(authentication.getName());
    }
}
