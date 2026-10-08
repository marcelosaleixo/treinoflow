package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.CentralInteligenciaService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CentralInteligenciaController {
    private final CentralInteligenciaService service;
    private final UsuarioPersonalService usuarioPersonalService;

    public CentralInteligenciaController(CentralInteligenciaService service,
                                         UsuarioPersonalService usuarioPersonalService) {
        this.service = service;
        this.usuarioPersonalService = usuarioPersonalService;
    }

    @GetMapping("/inteligencia")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuário não autenticado.");
        }
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("dashboard", service.dashboard(personal.getId()));
        return "inteligencia/index";
    }
}
