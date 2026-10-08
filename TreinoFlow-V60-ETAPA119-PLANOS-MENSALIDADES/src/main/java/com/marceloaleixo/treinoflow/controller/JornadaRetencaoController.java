package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.JornadaRetencaoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class JornadaRetencaoController {
    private final JornadaRetencaoService jornadas;
    private final UsuarioPersonalService usuarios;

    public JornadaRetencaoController(JornadaRetencaoService jornadas, UsuarioPersonalService usuarios) {
        this.jornadas = jornadas;
        this.usuarios = usuarios;
    }

    @GetMapping("/assistente/jornada")
    public String index(Authentication authentication, Model model) {
        UsuarioPersonal p = personal(authentication);
        model.addAttribute("personal", p);
        model.addAttribute("resumo", jornadas.resumo(p.getId()));
        return "assistente/jornada";
    }

    @PostMapping("/assistente/jornada/processar")
    public String processar(Authentication authentication, RedirectAttributes ra) {
        try {
            UsuarioPersonal p = personal(authentication);
            var r = jornadas.processar(p.getId());
            ra.addFlashAttribute("sucesso", r.mensagem() + " Avaliadas: " + r.avaliadas() + " · Executadas: " + r.executadas() + " · Encerradas: " + r.encerradas() + ".");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/assistente/jornada";
    }

    private UsuarioPersonal personal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) throw new IllegalArgumentException("Usuário não autenticado.");
        return usuarios.buscarPorEmail(authentication.getName());
    }
}
