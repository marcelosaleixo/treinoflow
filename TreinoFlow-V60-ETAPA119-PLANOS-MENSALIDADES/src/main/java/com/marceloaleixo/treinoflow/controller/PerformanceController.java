package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.DashboardPerformanceService;
import com.marceloaleixo.treinoflow.service.FeedbackTreinoInteligenteService;
import com.marceloaleixo.treinoflow.service.ProgressaoTreinoService;
import com.marceloaleixo.treinoflow.service.PosTreinoAcaoService;
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
    private final FeedbackTreinoInteligenteService feedbackInteligente;
    private final PosTreinoAcaoService posTreinoAcao;

    public PerformanceController(UsuarioPersonalService personals,
                                  DashboardPerformanceService performance,
                                  ProgressaoTreinoService progressao,
                                  FeedbackTreinoInteligenteService feedbackInteligente,
                                  PosTreinoAcaoService posTreinoAcao) {
        this.personals = personals;
        this.performance = performance;
        this.progressao = progressao;
        this.feedbackInteligente = feedbackInteligente;
        this.posTreinoAcao = posTreinoAcao;
    }

    @GetMapping("/performance")
    public String performance(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("performance", performance.montar(personal.getId()));
        model.addAttribute("feedbacksInteligentes", feedbackInteligente.recentes(personal.getId()));
        return "performance/index";
    }

    @GetMapping("/performance/pos-treino")
    public String posTreino(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("central", posTreinoAcao.montar(personal.getId()));
        return "performance/pos-treino";
    }

    @GetMapping("/performance/progressao")
    public String progressao(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("progressao", progressao.montar(personal.getId()));
        return "performance/progressao";
    }
}
