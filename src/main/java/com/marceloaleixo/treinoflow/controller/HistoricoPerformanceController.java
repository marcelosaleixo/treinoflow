package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.HistoricoPerformanceService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HistoricoPerformanceController {
    private final HistoricoPerformanceService service;
    private final UsuarioPersonalService usuarioPersonalService;

    public HistoricoPerformanceController(HistoricoPerformanceService service,
                                           UsuarioPersonalService usuarioPersonalService) {
        this.service = service;
        this.usuarioPersonalService = usuarioPersonalService;
    }

    @GetMapping("/gamificacao/historico")
    public String historico(Authentication authentication, Model model) {
        UsuarioPersonal personal = personal(authentication);
        model.addAttribute("personal", personal);
        model.addAttribute("dashboard", service.dashboard(personal.getId()));
        return "gamificacao/historico";
    }

    private UsuarioPersonal personal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuário não autenticado.");
        }
        return usuarioPersonalService.buscarPorEmail(authentication.getName());
    }
}
