package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.RetencaoAnalyticsService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/retencao/analytics")
public class RetencaoAnalyticsController {
    private final RetencaoAnalyticsService analyticsService;
    private final UsuarioPersonalService usuarioPersonalService;

    public RetencaoAnalyticsController(RetencaoAnalyticsService analyticsService,
                                       UsuarioPersonalService usuarioPersonalService) {
        this.analyticsService = analyticsService;
        this.usuarioPersonalService = usuarioPersonalService;
    }

    @GetMapping
    public String dashboard(Authentication authentication, Model model) {
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("analytics", analyticsService.dashboard(personal.getId()));
        return "retencao/analytics";
    }
}
