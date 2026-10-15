package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.service.FilaAlertaEmailService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/observabilidade/emails")
public class EntregaAlertaEmailAdminController {
    private final FilaAlertaEmailService fila;
    public EntregaAlertaEmailAdminController(FilaAlertaEmailService fila) { this.fila = fila; }

    @GetMapping
    public String painel(@RequestParam(required = false, defaultValue = "TODOS") String status,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
                         @RequestParam(defaultValue = "0") int pagina,
                         Model model) {
        String filtro = switch (status) { case "PENDENTE", "ENVIADO", "FALHA_FINAL" -> status; default -> "TODOS"; };
        LocalDate inicioSeguro = dataInicio;
        LocalDate fimSeguro = dataFim;
        if (inicioSeguro != null && fimSeguro != null && inicioSeguro.isAfter(fimSeguro)) {
            LocalDate temporaria = inicioSeguro; inicioSeguro = fimSeguro; fimSeguro = temporaria;
        }
        LocalDateTime inicio = inicioSeguro == null ? null : inicioSeguro.atStartOfDay();
        LocalDateTime fim = fimSeguro == null ? null : fimSeguro.plusDays(1).atStartOfDay();
        // Evita que parâmetros negativos na URL causem erros de paginação.
        int paginaSegura = Math.max(0, pagina);
        var entregas = fila.pesquisar(filtro, inicio, fim, paginaSegura, 20);
        model.addAttribute("entregas", entregas.getContent());
        model.addAttribute("paginaAtual", entregas.getNumber());
        model.addAttribute("totalPaginas", entregas.getTotalPages());
        model.addAttribute("totalElementos", entregas.getTotalElements());
        model.addAttribute("statusSelecionado", filtro);
        model.addAttribute("dataInicio", inicioSeguro);
        model.addAttribute("dataFim", fimSeguro);
        model.addAttribute("indicadores", fila.obterIndicadores());
        return "admin/observabilidade/emails";
    }

    @PostMapping("/{id}/reenviar")
    public String reenviar(@PathVariable String id, RedirectAttributes redirect) {
        if (fila.reenviar(id)) redirect.addFlashAttribute("sucesso", "Entrega colocada novamente na fila.");
        else redirect.addFlashAttribute("erro", "Não foi possível reenviar. Verifique se a entrega existe e está em falha final.");
        return "redirect:/admin/observabilidade/emails?status=FALHA_FINAL";
    }
}
