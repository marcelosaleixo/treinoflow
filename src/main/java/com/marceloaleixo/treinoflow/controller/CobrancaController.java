package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.CobrancaService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/assinatura")
public class CobrancaController {
    private final UsuarioPersonalService usuarios;
    private final CobrancaService cobrancas;
    public CobrancaController(UsuarioPersonalService usuarios, CobrancaService cobrancas) { this.usuarios = usuarios; this.cobrancas = cobrancas; }

    @GetMapping("/cobrancas")
    public String minhas(Authentication authentication, Model model) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        model.addAttribute("cobrancas", cobrancas.listarDoPersonal(personal.getId()));
        model.addAttribute("personal", personal);
        return "cobrancas/lista";
    }
}
