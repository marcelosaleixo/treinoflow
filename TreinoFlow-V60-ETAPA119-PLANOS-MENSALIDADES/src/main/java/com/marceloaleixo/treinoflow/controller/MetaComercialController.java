package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.MetaComercialService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MetaComercialController {
    private final MetaComercialService service;
    private final UsuarioPersonalService usuarioPersonalService;
    public MetaComercialController(MetaComercialService service, UsuarioPersonalService usuarioPersonalService) { this.service = service; this.usuarioPersonalService = usuarioPersonalService; }

    @GetMapping("/metas")
    public String dashboard(Authentication authentication, Model model) {
        UsuarioPersonal personal = personal(authentication); model.addAttribute("personal", personal);
        model.addAttribute("dashboard", service.dashboard(personal.getId())); return "metas/index";
    }

    @PostMapping("/metas")
    public String salvar(Authentication authentication, @RequestParam int metaAlunosAtivos,
                         @RequestParam int metaTreinos, @RequestParam int metaRecuperacoes,
                         RedirectAttributes redirect) {
        try { service.salvar(personal(authentication).getId(), metaAlunosAtivos, metaTreinos, metaRecuperacoes);
            redirect.addFlashAttribute("sucesso", "Metas do mês atualizadas com sucesso.");
        } catch (IllegalArgumentException e) { redirect.addFlashAttribute("erro", e.getMessage()); }
        return "redirect:/metas";
    }
    private UsuarioPersonal personal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) throw new IllegalArgumentException("Usuário não autenticado.");
        return usuarioPersonalService.buscarPorEmail(authentication.getName());
    }
}
