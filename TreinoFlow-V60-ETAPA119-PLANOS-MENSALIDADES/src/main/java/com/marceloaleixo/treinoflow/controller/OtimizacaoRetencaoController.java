package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.OtimizacaoRetencaoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OtimizacaoRetencaoController {
    private final OtimizacaoRetencaoService service;
    private final UsuarioPersonalService usuarios;

    public OtimizacaoRetencaoController(OtimizacaoRetencaoService service, UsuarioPersonalService usuarios) {
        this.service = service;
        this.usuarios = usuarios;
    }

    @GetMapping("/assistente/otimizacao")
    public String index(Authentication authentication, Model model) {
        UsuarioPersonal personal = personal(authentication);
        model.addAttribute("personal", personal);
        model.addAttribute("dashboard", service.dashboard(personal.getId()));
        return "assistente/otimizacao";
    }

    private UsuarioPersonal personal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuário não autenticado.");
        }
        return usuarios.buscarPorEmail(authentication.getName());
    }
}
