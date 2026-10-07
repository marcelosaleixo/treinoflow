package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.MensagemInteligenteService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MensagemInteligenteController {
    private final MensagemInteligenteService service;
    private final UsuarioPersonalService usuarios;

    public MensagemInteligenteController(MensagemInteligenteService service, UsuarioPersonalService usuarios) {
        this.service = service;
        this.usuarios = usuarios;
    }

    @GetMapping("/assistente/mensagens")
    public String index(Authentication authentication, Model model) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("dashboard", service.resumo(personal.getId()));
        return "assistente/mensagens";
    }
}
