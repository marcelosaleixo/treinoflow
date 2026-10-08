package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.PresencaService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/agenda/presenca")
public class PresencaController {
    private final PresencaService presenca;
    private final UsuarioPersonalService personals;

    public PresencaController(PresencaService presenca, UsuarioPersonalService personals) {
        this.presenca = presenca;
        this.personals = personals;
    }

    @GetMapping
    public String index(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("dashboard", presenca.montar(personal.getId()));
        return "agenda/presenca";
    }
}
