package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.AprendizadoAcoesAssistenteService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AprendizadoAcoesAssistenteController {
    private final AprendizadoAcoesAssistenteService service;
    private final UsuarioPersonalService usuarioPersonalService;

    public AprendizadoAcoesAssistenteController(AprendizadoAcoesAssistenteService service, UsuarioPersonalService usuarioPersonalService) {
        this.service = service;
        this.usuarioPersonalService = usuarioPersonalService;
    }

    @GetMapping("/assistente/aprendizado")
    public String index(Authentication authentication, Model model, @RequestParam(defaultValue = "90") int dias) {
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("dashboard", service.dashboard(personal.getId(), dias));
        model.addAttribute("dias", dias);
        return "assistente/aprendizado";
    }
}
