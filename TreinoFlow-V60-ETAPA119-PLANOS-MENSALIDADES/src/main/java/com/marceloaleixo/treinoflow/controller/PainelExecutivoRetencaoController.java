package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.PainelExecutivoRetencaoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PainelExecutivoRetencaoController {
    private final PainelExecutivoRetencaoService service;
    private final UsuarioPersonalService usuarioPersonalService;

    public PainelExecutivoRetencaoController(PainelExecutivoRetencaoService service,
                                             UsuarioPersonalService usuarioPersonalService) {
        this.service = service;
        this.usuarioPersonalService = usuarioPersonalService;
    }

    @GetMapping("/retencao/executivo")
    public String dashboard(Authentication authentication, Model model) {
        UsuarioPersonal personal = personal(authentication);
        model.addAttribute("personal", personal);
        model.addAttribute("dashboard", service.dashboard(personal.getId()));
        return "retencao/executivo";
    }

    private UsuarioPersonal personal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuário não autenticado.");
        }
        return usuarioPersonalService.buscarPorEmail(authentication.getName());
    }
}
