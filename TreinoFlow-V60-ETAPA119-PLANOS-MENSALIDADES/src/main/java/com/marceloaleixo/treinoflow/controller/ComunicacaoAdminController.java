package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.dto.TemplateNotificacaoForm;
import com.marceloaleixo.treinoflow.entity.TemplateNotificacao;
import com.marceloaleixo.treinoflow.enums.CanalNotificacao;
import com.marceloaleixo.treinoflow.enums.TipoNotificacao;
import com.marceloaleixo.treinoflow.repository.TemplateNotificacaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/comunicacao")
public class ComunicacaoAdminController {
    private final TemplateNotificacaoRepository templates;
    private final boolean emailEnabled;
    private final boolean whatsappEnabled;

    public ComunicacaoAdminController(TemplateNotificacaoRepository templates,
                                      @Value("${treinoflow.notificacoes.email.enabled:false}") boolean emailEnabled,
                                      @Value("${treinoflow.notificacoes.whatsapp.enabled:false}") boolean whatsappEnabled) {
        this.templates = templates;
        this.emailEnabled = emailEnabled;
        this.whatsappEnabled = whatsappEnabled;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("emailEnabled", emailEnabled);
        model.addAttribute("whatsappEnabled", whatsappEnabled);
        model.addAttribute("templates", templates.findAllByOrderByTipoAscCanalAsc());
        model.addAttribute("totalTemplates", templates.count());
        return "admin/comunicacao/index";
    }

    @GetMapping("/templates/editar")
    public String editar(@RequestParam TipoNotificacao tipo,
                         @RequestParam CanalNotificacao canal,
                         Model model) {
        TemplateNotificacaoForm form = new TemplateNotificacaoForm();
        templates.findByTipoAndCanal(tipo, canal).ifPresent(t -> {
            form.setId(t.getId());
            form.setTitulo(t.getTitulo());
            form.setMensagem(t.getMensagem());
            form.setAtivo(t.isAtivo());
        });
        form.setTipo(tipo);
        form.setCanal(canal);
        model.addAttribute("form", form);
        model.addAttribute("tipos", TipoNotificacao.values());
        model.addAttribute("canais", CanalNotificacao.values());
        return "admin/comunicacao/template-form";
    }

    @PostMapping("/templates/salvar")
    public String salvar(@ModelAttribute("form") TemplateNotificacaoForm form,
                         RedirectAttributes ra) {
        if (form.getTipo() == null || form.getCanal() == null) {
            ra.addFlashAttribute("erro", "Informe o evento e o canal do template.");
            return "redirect:/admin/comunicacao";
        }
        if (form.getTitulo() == null || form.getTitulo().isBlank()) {
            ra.addFlashAttribute("erro", "Informe o título do template.");
            return "redirect:/admin/comunicacao/templates/editar?tipo=" + form.getTipo() + "&canal=" + form.getCanal();
        }
        if (form.getMensagem() == null || form.getMensagem().isBlank()) {
            ra.addFlashAttribute("erro", "Informe a mensagem do template.");
            return "redirect:/admin/comunicacao/templates/editar?tipo=" + form.getTipo() + "&canal=" + form.getCanal();
        }
        TemplateNotificacao t = templates.findByTipoAndCanal(form.getTipo(), form.getCanal()).orElseGet(TemplateNotificacao::new);
        t.setTipo(form.getTipo());
        t.setCanal(form.getCanal());
        t.setTitulo(form.getTitulo().trim());
        t.setMensagem(form.getMensagem().trim());
        t.setAtivo(form.isAtivo());
        templates.save(t);
        ra.addFlashAttribute("sucesso", "Template de comunicação salvo.");
        return "redirect:/admin/comunicacao";
    }
}
