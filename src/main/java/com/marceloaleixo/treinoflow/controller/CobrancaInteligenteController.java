package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.CanalCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.service.CobrancaInteligenteService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/** Etapa 123: fila inteligente de cobrança integrada ao CRM. */
@Controller
@RequestMapping("/financeiro/cobranca-inteligente")
public class CobrancaInteligenteController {
    private final UsuarioPersonalService personals;
    private final CobrancaInteligenteService cobrancas;

    public CobrancaInteligenteController(UsuarioPersonalService personals,
                                         CobrancaInteligenteService cobrancas) {
        this.personals = personals;
        this.cobrancas = cobrancas;
    }

    @GetMapping
    public String index(Authentication authentication, Model model) {
        UsuarioPersonal personal = personal(authentication);
        model.addAttribute("personal", personal);
        model.addAttribute("cobrancas", cobrancas.listar(personal.getId()));
        return "financeiro/cobranca-inteligente";
    }

    @GetMapping("/conta/{contaId}")
    public String abrir(@PathVariable Long contaId, Authentication authentication, Model model) {
        UsuarioPersonal personal = personal(authentication);
        model.addAttribute("personal", personal);
        model.addAttribute("cobranca", cobrancas.preparar(personal.getId(), contaId));
        model.addAttribute("canais", CanalCrm.values());
        model.addAttribute("resultados", ResultadoCrm.values());
        model.addAttribute("hojeData", LocalDate.now());
        return "financeiro/cobranca-form";
    }

    @PostMapping("/conta/{contaId}/registrar")
    public String registrar(@PathVariable Long contaId,
                            @RequestParam CanalCrm canal,
                            @RequestParam ResultadoCrm resultado,
                            @RequestParam(required = false) LocalDate proximaAcao,
                            @RequestParam(required = false) String observacao,
                            Authentication authentication,
                            RedirectAttributes ra) {
        try {
            UsuarioPersonal personal = personal(authentication);
            cobrancas.registrar(personal.getId(), contaId, canal, resultado, proximaAcao, observacao);
            ra.addFlashAttribute("sucesso", "Cobrança registrada no CRM com sucesso.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/financeiro/cobranca-inteligente/conta/" + contaId;
        }
        return "redirect:/financeiro/cobranca-inteligente";
    }

    private UsuarioPersonal personal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuário não autenticado.");
        }
        return personals.buscarPorEmail(authentication.getName());
    }
}
