package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.enums.StatusCobranca;
import com.marceloaleixo.treinoflow.repository.CobrancaRepository;
import com.marceloaleixo.treinoflow.service.CobrancaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/cobrancas")
public class CobrancaAdminController {
    private final CobrancaRepository cobrancas;
    private final CobrancaService service;

    public CobrancaAdminController(CobrancaRepository cobrancas, CobrancaService service) {
        this.cobrancas = cobrancas; this.service = service;
    }

    @GetMapping
    public String lista(Model model) {
        service.atualizarAtrasadas();
        model.addAttribute("cobrancas", service.listarTodas());
        model.addAttribute("pendentes", cobrancas.countByStatus(StatusCobranca.PENDENTE));
        model.addAttribute("atrasadas", cobrancas.countByStatus(StatusCobranca.ATRASADA));
        model.addAttribute("pagas", cobrancas.countByStatus(StatusCobranca.PAGA));
        model.addAttribute("recebido", cobrancas.somarPorStatus(StatusCobranca.PAGA));
        return "admin/cobrancas/lista";
    }

    @PostMapping("/gerar")
    public String gerar(RedirectAttributes ra) {
        int total = service.gerarCobrancasDoMes();
        ra.addFlashAttribute("sucesso", total + " cobrança(s) gerada(s) para o mês atual.");
        return "redirect:/admin/cobrancas";
    }

    @PostMapping("/{id}/pagar")
    public String pagar(@PathVariable Long id, RedirectAttributes ra) {
        service.marcarPaga(id);
        ra.addFlashAttribute("sucesso", "Cobrança marcada como paga e próxima cobrança atualizada.");
        return "redirect:/admin/cobrancas";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes ra) {
        service.cancelar(id);
        ra.addFlashAttribute("sucesso", "Cobrança cancelada.");
        return "redirect:/admin/cobrancas";
    }
}
