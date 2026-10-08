package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.AcoesInteligentesService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/retencao/acoes-inteligentes")
public class AcoesInteligentesController {
    private final AcoesInteligentesService service;
    private final UsuarioPersonalService usuarioPersonalService;

    public AcoesInteligentesController(AcoesInteligentesService service,
                                       UsuarioPersonalService usuarioPersonalService) {
        this.service = service;
        this.usuarioPersonalService = usuarioPersonalService;
    }

    @GetMapping
    public String index(Authentication authentication, Model model) {
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        var acoes = service.listar(personal.getId());
        model.addAttribute("personal", personal);
        model.addAttribute("acoes", acoes);
        model.addAttribute("urgentes", acoes.stream().filter(a -> "URGENTE".equals(a.prioridade())).count());
        model.addAttribute("altas", acoes.stream().filter(a -> "ALTA".equals(a.prioridade())).count());
        model.addAttribute("medias", acoes.stream().filter(a -> "MEDIA".equals(a.prioridade())).count());
        return "retencao/acoes-inteligentes";
    }
}
