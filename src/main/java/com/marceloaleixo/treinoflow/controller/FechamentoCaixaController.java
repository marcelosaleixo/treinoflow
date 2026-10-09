package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.FechamentoCaixaService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.YearMonth;

@Controller
@RequestMapping("/financeiro/fechamento")
public class FechamentoCaixaController {
    private final UsuarioPersonalService personals;
    private final FechamentoCaixaService fechamento;

    public FechamentoCaixaController(UsuarioPersonalService personals, FechamentoCaixaService fechamento) {
        this.personals = personals;
        this.fechamento = fechamento;
    }

    @GetMapping
    public String index(Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        YearMonth periodo = mes == null ? YearMonth.now() : mes;
        model.addAttribute("personal", personal);
        model.addAttribute("mes", periodo.toString());
        model.addAttribute("resumo", fechamento.montar(personal.getId(), periodo));
        return "financeiro/fechamento";
    }
}
