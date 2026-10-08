package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.AprendizadoRetencaoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Etapa 80: painel transparente do aprendizado contínuo. */
@Controller
public class AprendizadoRetencaoController {

    private final AprendizadoRetencaoService aprendizado;
    private final UsuarioPersonalService usuarios;

    public AprendizadoRetencaoController(AprendizadoRetencaoService aprendizado,
                                         UsuarioPersonalService usuarios) {
        this.aprendizado = aprendizado;
        this.usuarios = usuarios;
    }

    @GetMapping("/assistente/aprendizado")
    public String index(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuário não autenticado.");
        }
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("dashboard", aprendizado.dashboard(personal.getId()));
        return "assistente/aprendizado";
    }
}
