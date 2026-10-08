package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.RadarDiarioService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RadarDiarioController {
    private final UsuarioPersonalService personals;
    private final RadarDiarioService radar;

    public RadarDiarioController(UsuarioPersonalService personals, RadarDiarioService radar) {
        this.personals = personals;
        this.radar = radar;
    }

    @GetMapping("/performance/radar-diario")
    public String index(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("radar", radar.montar(personal.getId()));
        return "performance/radar-diario";
    }
}
