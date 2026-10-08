package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.ScoreRiscoAlunoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/retencao/risco")
public class ScoreRiscoAlunoController {
    private final ScoreRiscoAlunoService service;
    private final UsuarioPersonalService personals;

    public ScoreRiscoAlunoController(ScoreRiscoAlunoService service, UsuarioPersonalService personals) {
        this.service = service;
        this.personals = personals;
    }

    @GetMapping
    public String index(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        var alunos = service.listar(personal.getId());
        model.addAttribute("personal", personal);
        model.addAttribute("alunos", alunos);
        model.addAttribute("criticos", alunos.stream().filter(a -> "CRITICO".equals(a.nivel())).count());
        model.addAttribute("altos", alunos.stream().filter(a -> "ALTO".equals(a.nivel())).count());
        model.addAttribute("medios", alunos.stream().filter(a -> "MEDIO".equals(a.nivel())).count());
        model.addAttribute("baixos", alunos.stream().filter(a -> "BAIXO".equals(a.nivel())).count());
        model.addAttribute("risco", alunos.stream().filter(a -> a.score() >= 50).count());
        return "retencao/risco";
    }
}
