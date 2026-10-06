package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.RecomendacaoAdaptativaDashboardService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RecomendacaoAdaptativaController {
    private final RecomendacaoAdaptativaDashboardService dashboardService;
    private final UsuarioPersonalService usuarioPersonalService;

    public RecomendacaoAdaptativaController(RecomendacaoAdaptativaDashboardService dashboardService,
                                             UsuarioPersonalService usuarioPersonalService) {
        this.dashboardService = dashboardService;
        this.usuarioPersonalService = usuarioPersonalService;
    }

    @GetMapping("/assistente/recomendacoes")
    public String index(Authentication authentication, Model model) {
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("dashboard", dashboardService.dashboard(personal.getId()));
        return "assistente/recomendacoes";
    }
}
