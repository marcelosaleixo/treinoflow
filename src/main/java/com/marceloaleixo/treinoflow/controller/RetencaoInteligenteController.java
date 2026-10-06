package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.RetencaoInteligenteService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RetencaoInteligenteController {
    private final RetencaoInteligenteService service;
    private final UsuarioPersonalService usuarioPersonalService;

    public RetencaoInteligenteController(RetencaoInteligenteService service,
                                         UsuarioPersonalService usuarioPersonalService) {
        this.service = service;
        this.usuarioPersonalService = usuarioPersonalService;
    }

    @GetMapping("/retencao/central")
    public String central(Authentication authentication, Model model) {
        UsuarioPersonal personal = personal(authentication);
        model.addAttribute("personal", personal);
        var acoes = service.listar(personal.getId());
        model.addAttribute("acoes", acoes);
        model.addAttribute("criticosAltos", acoes.stream().filter(a -> a.risco().score() >= 50).count());
        return "retencao/central";
    }

    @PostMapping("/retencao/central/{alunoId}/whatsapp")
    public String enviarWhatsApp(@PathVariable Long alunoId, Authentication authentication, RedirectAttributes ra) {
        try {
            UsuarioPersonal personal = personal(authentication);
            service.enviarWhatsApp(personal.getId(), alunoId);
            ra.addFlashAttribute("sucesso", "WhatsApp enviado e interação registrada no CRM.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        } catch (Exception ex) {
            ra.addFlashAttribute("erro", "Não foi possível enviar o WhatsApp. Verifique a configuração da integração.");
        }
        return "redirect:/retencao/central";
    }

    @PostMapping("/retencao/central/{alunoId}/follow-up")
    public String criarFollowUp(@PathVariable Long alunoId, Authentication authentication, RedirectAttributes ra) {
        try {
            UsuarioPersonal personal = personal(authentication);
            service.criarFollowUp(personal.getId(), alunoId);
            ra.addFlashAttribute("sucesso", "Follow-up criado no CRM.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/retencao/central";
    }

    private UsuarioPersonal personal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuário não autenticado.");
        }
        return usuarioPersonalService.buscarPorEmail(authentication.getName());
    }
}
