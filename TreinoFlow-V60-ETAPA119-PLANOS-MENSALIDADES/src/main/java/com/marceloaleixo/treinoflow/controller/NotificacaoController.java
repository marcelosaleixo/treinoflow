package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.NotificacaoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notificacoes")
public class NotificacaoController {
    private final UsuarioPersonalService usuarios;
    private final NotificacaoService notificacoes;

    public NotificacaoController(UsuarioPersonalService usuarios, NotificacaoService notificacoes) {
        this.usuarios = usuarios;
        this.notificacoes = notificacoes;
    }

    @GetMapping
    public String lista(Authentication authentication, Model model) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        model.addAttribute("notificacoes", notificacoes.listar(personal.getId()));
        return "notificacoes/lista";
    }

    @PostMapping("/{id}/ler")
    public String ler(@PathVariable Long id, Authentication authentication) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        notificacoes.marcarLida(personal.getId(), id);
        return "redirect:/notificacoes";
    }

    @PostMapping("/ler-todas")
    public String lerTodas(Authentication authentication) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        notificacoes.marcarTodasLidas(personal.getId());
        return "redirect:/notificacoes";
    }
}
