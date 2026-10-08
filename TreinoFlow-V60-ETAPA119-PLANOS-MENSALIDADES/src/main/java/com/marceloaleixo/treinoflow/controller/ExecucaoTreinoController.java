package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.ExecucaoTreinoDashboardService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExecucaoTreinoController {
    private final UsuarioPersonalService personals;
    private final ExecucaoTreinoDashboardService dashboard;

    public ExecucaoTreinoController(UsuarioPersonalService personals, ExecucaoTreinoDashboardService dashboard) {
        this.personals = personals;
        this.dashboard = dashboard;
    }

    @GetMapping("/performance/execucao")
    public String execucao(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("execucao", dashboard.montar(personal.getId()));
        return "performance/execucao";
    }
}
