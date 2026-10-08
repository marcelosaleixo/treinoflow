package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.RetencaoFaltasService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/retencao/faltas")
public class RetencaoFaltasController {
    private final RetencaoFaltasService service;
    private final UsuarioPersonalService personals;

    public RetencaoFaltasController(RetencaoFaltasService service, UsuarioPersonalService personals) {
        this.service = service;
        this.personals = personals;
    }

    @GetMapping
    public String index(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("riscos", service.listarRiscos(personal.getId()));
        return "retencao/faltas";
    }

    @PostMapping("/gerar")
    public String gerar(Authentication authentication, RedirectAttributes ra) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        try {
            int quantidade = service.gerarAcoes(personal.getId());
            ra.addFlashAttribute("sucesso", quantidade == 0
                    ? "Nenhuma nova ação foi criada. A fila existente continua protegida contra duplicidade."
                    : quantidade + " ação(ões) de retenção por faltas criada(s) no CRM.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/retencao/faltas";
    }
}
