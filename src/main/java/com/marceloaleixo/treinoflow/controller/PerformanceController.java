package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.DashboardPerformanceService;
import com.marceloaleixo.treinoflow.service.ProgressaoTreinoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PerformanceController {
    private final UsuarioPersonalService personals;
    private final DashboardPerformanceService performance;
    private final ProgressaoTreinoService progressao;

    public PerformanceController(UsuarioPersonalService personals,
                                  DashboardPerformanceService performance,
                                  ProgressaoTreinoService progressao) {
        this.personals = personals;
        this.performance = performance;
        this.progressao = progressao;
    }

    @GetMapping("/performance")
    public String performance(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("performance", performance.montar(personal.getId()));
        return "performance/index";
    }

    @GetMapping("/performance/progressao")
    public String progressao(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("progressao", progressao.montar(personal.getId()));
        return "performance/progressao";
    }
}
