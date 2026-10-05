package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.RetencaoService;
import com.marceloaleixo.treinoflow.service.AutomacaoRetencaoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;

import java.time.LocalDate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/retencao")
public class RetencaoController {
    private final RetencaoService retencaoService;
    private final UsuarioPersonalService usuarioPersonalService;
    private final AutomacaoRetencaoService automacaoRetencaoService;

    public RetencaoController(RetencaoService retencaoService, UsuarioPersonalService usuarioPersonalService,
                              AutomacaoRetencaoService automacaoRetencaoService) {
        this.retencaoService = retencaoService;
        this.usuarioPersonalService = usuarioPersonalService;
        this.automacaoRetencaoService = automacaoRetencaoService;
    }

    @GetMapping
    public String dashboard(Authentication authentication, Model model) {
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("retencao", retencaoService.dashboard(personal.getId()));
        return "retencao/index";
    }

    @PostMapping("/gerar")
    public String gerar(Authentication authentication, RedirectAttributes ra) {
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        int quantidade = automacaoRetencaoService.gerarParaPersonal(personal.getId());
        ra.addFlashAttribute("sucesso", quantidade == 0
                ? "Nenhuma nova ação automática foi criada. As ações existentes continuam na fila."
                : quantidade + " ação(ões) de retenção criada(s) automaticamente.");
        return "redirect:/retencao";
    }

    @PostMapping("/interacoes/{id}/resultado")
    public String atualizarResultado(@PathVariable Long id,
                                     @RequestParam ResultadoCrm resultado,
                                     @RequestParam(required = false) String proximaAcao,
                                     Authentication authentication,
                                     RedirectAttributes ra) {
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        try {
            LocalDate data = (proximaAcao == null || proximaAcao.isBlank())
                    ? null : LocalDate.parse(proximaAcao);
            retencaoService.atualizarResultado(personal.getId(), id, resultado, data);
            ra.addFlashAttribute("sucesso", "Resultado do contato atualizado com sucesso.");
        } catch (Exception ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/retencao";
    }

    @GetMapping("/whatsapp/{id}")
    public String whatsapp(@PathVariable Long id, Authentication authentication, RedirectAttributes ra) {
        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        try {
            return "redirect:" + retencaoService.linkWhatsApp(personal.getId(), id);
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/retencao";
        }
    }
}
