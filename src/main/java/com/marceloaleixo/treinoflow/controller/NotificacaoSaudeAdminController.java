package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.service.AlertaOperacionalNotificacaoService;
import com.marceloaleixo.treinoflow.service.ObservabilidadeNotificacaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/observabilidade/notificacoes")
public class NotificacaoSaudeAdminController {
    private final ObservabilidadeNotificacaoService metricas;
    private final AlertaOperacionalNotificacaoService alertas;

    public NotificacaoSaudeAdminController(ObservabilidadeNotificacaoService metricas,
                                           AlertaOperacionalNotificacaoService alertas) {
        this.metricas = metricas;
        this.alertas = alertas;
    }

    @GetMapping
    public String painel(Model model) {
        model.addAttribute("metricas", metricas.obterResumo());
        model.addAttribute("incidentes", alertas.listarRecentes());
        model.addAttribute("incidentesAbertos", alertas.contarAbertos());
        return "admin/observabilidade/notificacoes";
    }

    @PostMapping("/incidentes/{id}/resolver")
    public String resolver(@PathVariable String id, RedirectAttributes redirectAttributes) {
        if (alertas.resolver(id)) {
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Incidente marcado como resolvido.");
        } else {
            redirectAttributes.addFlashAttribute("mensagemErro", "O incidente não foi encontrado ou já foi resolvido.");
        }
        return "redirect:/admin/observabilidade/notificacoes";
    }
}
