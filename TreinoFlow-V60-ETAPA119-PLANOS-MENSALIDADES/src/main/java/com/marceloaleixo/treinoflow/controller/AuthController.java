package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.dto.CadastroPersonalForm;
import com.marceloaleixo.treinoflow.service.AutenticacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private AutenticacaoService autenticacaoService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/cadastro")
    public String cadastroForm(Model model) {
        model.addAttribute("form", new CadastroPersonalForm());
        return "auth/cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("form") CadastroPersonalForm form,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/cadastro";
        }
        try {
            autenticacaoService.cadastrar(form);
            return "redirect:/login?cadastroSucesso";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("erro", ex.getMessage());
            return "auth/cadastro";
        }
    }
}
