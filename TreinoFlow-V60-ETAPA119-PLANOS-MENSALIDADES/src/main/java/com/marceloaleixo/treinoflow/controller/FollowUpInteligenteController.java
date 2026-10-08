package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.dto.FollowUpInteligenteView;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.service.FollowUpInteligenteService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class FollowUpInteligenteController {
    private final UsuarioPersonalService personals;
    private final FollowUpInteligenteService followUps;

    public FollowUpInteligenteController(UsuarioPersonalService personals,
                                         FollowUpInteligenteService followUps) {
        this.personals = personals;
        this.followUps = followUps;
    }

    @GetMapping("/performance/follow-up-inteligente")
    public String index(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        var lista = followUps.listar(personal.getId());
        model.addAttribute("personal", personal);
        model.addAttribute("followUps", lista);
        model.addAttribute("vencidos", lista.stream().filter(v -> "VENCIDO".equals(v.statusPrazo())).count());
        model.addAttribute("hoje", lista.stream().filter(v -> "HOJE".equals(v.statusPrazo())).count());
        model.addAttribute("proximos", lista.stream().filter(v -> "PRÓXIMO".equals(v.statusPrazo())).count());
        model.addAttribute("resultados", ResultadoCrm.values());
        model.addAttribute("hojeData", LocalDate.now());
        return "performance/follow-up-inteligente";
    }

    @PostMapping("/performance/follow-up-inteligente/{interacaoId}/resolver")
    public String resolver(@PathVariable Long interacaoId,
                           @RequestParam ResultadoCrm resultado,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate proximaAcao,
                           @RequestParam(required = false) String observacao,
                           Authentication authentication,
                           RedirectAttributes ra) {
        try {
            UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
            followUps.resolver(personal.getId(), interacaoId, resultado, proximaAcao, observacao);
            ra.addFlashAttribute("sucesso", "Follow-up registrado. O próximo passo foi atualizado.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/performance/follow-up-inteligente";
    }
}
