package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.ConciliacaoFinanceiraService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.YearMonth;

@Controller
@RequestMapping("/financeiro/conciliacao")
public class ConciliacaoFinanceiraController {
    private final UsuarioPersonalService personals;
    private final ConciliacaoFinanceiraService conciliacao;
    public ConciliacaoFinanceiraController(UsuarioPersonalService personals, ConciliacaoFinanceiraService conciliacao) {
        this.personals = personals; this.conciliacao = conciliacao;
    }

    @GetMapping
    public String index(Authentication authentication, @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        YearMonth periodo = mes == null ? YearMonth.now() : mes;
        model.addAttribute("personal", personal);
        model.addAttribute("mes", periodo.toString());
        model.addAttribute("linhas", conciliacao.listar(personal.getId(), periodo));
        return "financeiro/conciliacao";
    }

    @PostMapping("/{pagamentoId}")
    public String conciliar(Authentication authentication, @PathVariable Long pagamentoId,
            @RequestParam(required = false) String observacao, @RequestParam(required = false) String mes,
            RedirectAttributes redirect) {
        try {
            UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
            conciliacao.conciliar(personal.getId(), pagamentoId, observacao);
            redirect.addFlashAttribute("sucesso", "Pagamento conciliado com sucesso.");
        } catch (Exception ex) { redirect.addFlashAttribute("erro", ex.getMessage()); }
        return "redirect:/financeiro/conciliacao" + (mes == null || mes.isBlank() ? "" : "?mes=" + mes);
    }
}
