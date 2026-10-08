package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.AssistenteAcoesService;
import com.marceloaleixo.treinoflow.service.RetencaoInteligenteService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AssistenteAcoesController {
    private final AssistenteAcoesService assistente;
    private final RetencaoInteligenteService retencao;
    private final UsuarioPersonalService usuarioPersonalService;

    public AssistenteAcoesController(AssistenteAcoesService assistente,
                                     RetencaoInteligenteService retencao,
                                     UsuarioPersonalService usuarioPersonalService) {
        this.assistente = assistente;
        this.retencao = retencao;
        this.usuarioPersonalService = usuarioPersonalService;
    }

    @GetMapping("/assistente/acoes")
    public String index(Authentication authentication, Model model) {
        UsuarioPersonal personal = personal(authentication);
        var acoes = assistente.listar(personal.getId());
        model.addAttribute("personal", personal);
        model.addAttribute("acoes", acoes);
        model.addAttribute("criticas", acoes.stream().filter(a -> "CRÍTICA".equals(a.prioridade())).count());
        model.addAttribute("altas", acoes.stream().filter(a -> "ALTA".equals(a.prioridade())).count());
        model.addAttribute("total", acoes.size());
        return "assistente/acoes";
    }

    @PostMapping("/assistente/acoes/{alunoId}/whatsapp")
    public String whatsapp(@PathVariable Long alunoId, Authentication authentication, RedirectAttributes ra) {
        try {
            UsuarioPersonal personal = personal(authentication);
            retencao.enviarWhatsApp(personal.getId(), alunoId);
            ra.addFlashAttribute("sucesso", "WhatsApp enviado e interação registrada no CRM.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        } catch (Exception ex) {
            ra.addFlashAttribute("erro", "Não foi possível enviar o WhatsApp. Verifique a configuração da integração.");
        }
        return "redirect:/assistente/acoes";
    }

    @PostMapping("/assistente/acoes/{alunoId}/follow-up")
    public String followUp(@PathVariable Long alunoId, Authentication authentication, RedirectAttributes ra) {
        try {
            UsuarioPersonal personal = personal(authentication);
            retencao.criarFollowUp(personal.getId(), alunoId);
            ra.addFlashAttribute("sucesso", "Follow-up criado no CRM.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/assistente/acoes";
    }

    private UsuarioPersonal personal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuário não autenticado.");
        }
        return usuarioPersonalService.buscarPorEmail(authentication.getName());
    }
}
