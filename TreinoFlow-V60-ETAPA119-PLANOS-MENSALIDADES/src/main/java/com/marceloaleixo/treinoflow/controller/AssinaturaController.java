package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.Assinatura;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.AssinaturaService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/assinatura")
public class AssinaturaController {

    private final UsuarioPersonalService usuarioPersonalService;
    private final AssinaturaService assinaturaService;

    public AssinaturaController(UsuarioPersonalService usuarioPersonalService,
                                 AssinaturaService assinaturaService) {
        this.usuarioPersonalService = usuarioPersonalService;
        this.assinaturaService = assinaturaService;
    }

    @GetMapping
    public String status(Authentication authentication, Model model) {
        return carregar(authentication, model, "assinatura/status");
    }

    @GetMapping("/bloqueada")
    public String bloqueada(Authentication authentication, Model model) {
        return carregar(authentication, model, "assinatura/bloqueada");
    }

    private String carregar(Authentication authentication, Model model, String view) {
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        if (personal != null && personal.getId() != null) {
            assinaturaService.atualizarVencida(personal.getId());
            Assinatura assinatura = assinaturaService.buscarPorPersonal(personal.getId()).orElse(null);
            model.addAttribute("personal", personal);
            model.addAttribute("assinatura", assinatura);
        }
        return view;
    }
}
