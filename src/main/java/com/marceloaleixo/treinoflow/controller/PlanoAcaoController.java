package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.PlanoAcaoAlunoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PlanoAcaoController {
    private final UsuarioPersonalService personals;
    private final PlanoAcaoAlunoService planoAcao;

    public PlanoAcaoController(UsuarioPersonalService personals, PlanoAcaoAlunoService planoAcao) {
        this.personals = personals;
        this.planoAcao = planoAcao;
    }

    @GetMapping("/performance/acoes")
    public String acoes(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        var acoes = planoAcao.listar(personal.getId());
        model.addAttribute("acoes", acoes);
        model.addAttribute("acoesAlta", acoes.stream().filter(a -> "ALTA".equals(a.prioridade())).count());
        model.addAttribute("acoesMedia", acoes.stream().filter(a -> "MEDIA".equals(a.prioridade())).count());
        return "performance/acoes";
    }
}
