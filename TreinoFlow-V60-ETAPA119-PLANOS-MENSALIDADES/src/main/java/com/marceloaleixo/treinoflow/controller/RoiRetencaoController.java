package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.RoiRetencaoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RoiRetencaoController {
    private final RoiRetencaoService service; private final UsuarioPersonalService usuarios;
    public RoiRetencaoController(RoiRetencaoService service, UsuarioPersonalService usuarios){this.service=service;this.usuarios=usuarios;}
    @GetMapping("/assistente/roi")
    public String index(Authentication authentication, Model model, @RequestParam(defaultValue="30") int dias){
        UsuarioPersonal p=personal(authentication); int periodo=dias==90?90:dias==365?365:30;
        model.addAttribute("personal",p); model.addAttribute("dias",periodo); model.addAttribute("dashboard",service.dashboard(p.getId(),periodo)); return "assistente/roi";
    }
    private UsuarioPersonal personal(Authentication a){if(a==null||!a.isAuthenticated())throw new IllegalArgumentException("Usuário não autenticado.");return usuarios.buscarPorEmail(a.getName());}
}
