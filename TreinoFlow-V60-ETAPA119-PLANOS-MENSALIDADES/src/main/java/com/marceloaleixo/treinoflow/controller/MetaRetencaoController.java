package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.MetaRetencaoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MetaRetencaoController {
    private final MetaRetencaoService service;
    private final UsuarioPersonalService usuarioPersonalService;

    public MetaRetencaoController(MetaRetencaoService service, UsuarioPersonalService usuarioPersonalService) {
        this.service = service; this.usuarioPersonalService = usuarioPersonalService;
    }

    @GetMapping("/retencao/metas")
    public String dashboard(Authentication authentication, Model model) {
        UsuarioPersonal personal = personal(authentication);
        model.addAttribute("personal", personal);
        model.addAttribute("dashboard", service.dashboard(personal.getId()));
        return "retencao/metas";
    }

    @PostMapping("/retencao/metas")
    public String salvar(Authentication authentication, @RequestParam int metaRecuperacoes,
                         @RequestParam double metaTaxa, RedirectAttributes redirect) {
        try {
            service.salvarMeta(personal(authentication).getId(), metaRecuperacoes, metaTaxa);
            redirect.addFlashAttribute("sucesso", "Meta de retenção atualizada com sucesso.");
        } catch (IllegalArgumentException e) { redirect.addFlashAttribute("erro", e.getMessage()); }
        return "redirect:/retencao/metas";
    }

    private UsuarioPersonal personal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) throw new IllegalArgumentException("Usuário não autenticado.");
        return usuarioPersonalService.buscarPorEmail(authentication.getName());
    }
}
