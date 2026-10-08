package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.SegmentacaoCarteiraService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/retencao/carteira")
public class SegmentacaoCarteiraController {
    private final SegmentacaoCarteiraService service;
    private final UsuarioPersonalService usuarioPersonalService;

    public SegmentacaoCarteiraController(SegmentacaoCarteiraService service,
                                         UsuarioPersonalService usuarioPersonalService) {
        this.service = service;
        this.usuarioPersonalService = usuarioPersonalService;
    }

    @GetMapping
    public String dashboard(Authentication authentication, Model model) {
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("carteira", service.dashboard(personal.getId()));
        return "retencao/carteira";
    }
}
