package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.DespesaPersonalService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.marceloaleixo.treinoflow.entity.DespesaPersonal;
import com.marceloaleixo.treinoflow.service.RelatorioCsvService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Controller
@RequestMapping("/financeiro/despesas")
public class DespesaPersonalController {
    private final UsuarioPersonalService personals;
    private final DespesaPersonalService despesas;
    private final RelatorioCsvService csv;
    public DespesaPersonalController(UsuarioPersonalService personals, DespesaPersonalService despesas, RelatorioCsvService csv) {
        this.personals = personals; this.despesas = despesas; this.csv = csv;
    }
    @GetMapping
    public String index(Authentication auth, @RequestParam(required = false) Integer ano,
                        @RequestParam(required = false) Integer mes, Model model) {
        UsuarioPersonal p = personals.buscarPorEmail(auth.getName());
        YearMonth periodo = (ano != null && mes != null && mes >= 1 && mes <= 12)
                ? YearMonth.of(ano, mes) : YearMonth.now();
        LocalDate inicio = periodo.atDay(1), fim = periodo.atEndOfMonth();
        model.addAttribute("personal", p); model.addAttribute("periodo", periodo);
        model.addAttribute("despesas", despesas.listar(p.getId(), inicio, fim));
        model.addAttribute("totalPago", despesas.totalPago(p.getId(), inicio, fim));
        model.addAttribute("totalPendente", despesas.totalPendente(p.getId(), inicio, fim));
        model.addAttribute("hoje", LocalDate.now());
        return "financeiro/despesas";
    }
    @GetMapping(value = "/exportar", produces = "text/csv")
    public ResponseEntity<byte[]> exportar(Authentication auth, @RequestParam(required = false) Integer ano,
                                           @RequestParam(required = false) Integer mes) {
        UsuarioPersonal p = personals.buscarPorEmail(auth.getName());
        YearMonth periodo = (ano != null && mes != null && mes >= 1 && mes <= 12)
                ? YearMonth.of(ano, mes) : YearMonth.now();
        StringBuilder conteudo = new StringBuilder();
        conteudo.append(csv.linha("Data", "Descrição", "Categoria", "Valor (R$)", "Situação", "Observação"));
        for (DespesaPersonal d : despesas.listar(p.getId(), periodo.atDay(1), periodo.atEndOfMonth())) {
            conteudo.append(csv.linha(csv.data(d.getDataDespesa()), d.getDescricao(), d.getCategoria(),
                    csv.dinheiro(d.getValor()), d.isPaga() ? "Paga" : "Pendente", d.getObservacao()));
        }
        String nome = "treinoflow-despesas-" + periodo + ".csv";
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nome)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8")).body(csv.bytes(conteudo.toString()));
    }

    @PostMapping
    public String salvar(Authentication auth, @RequestParam String descricao, @RequestParam String categoria,
                         @RequestParam BigDecimal valor, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDespesa,
                         @RequestParam(defaultValue = "false") boolean paga, @RequestParam(required = false) String observacao,
                         RedirectAttributes redirect) {
        try {
            UsuarioPersonal p = personals.buscarPorEmail(auth.getName());
            despesas.criar(p.getId(), descricao, categoria, valor, dataDespesa, paga, observacao);
            redirect.addFlashAttribute("sucesso", "Despesa registrada com sucesso.");
        } catch (Exception e) { redirect.addFlashAttribute("erro", e.getMessage()); }
        return "redirect:/financeiro/despesas";
    }
    @PostMapping("/{id}/excluir")
    public String excluir(Authentication auth, @PathVariable Long id, RedirectAttributes redirect) {
        try { despesas.excluir(personals.buscarPorEmail(auth.getName()).getId(), id); redirect.addFlashAttribute("sucesso", "Despesa excluída."); }
        catch (Exception e) { redirect.addFlashAttribute("erro", e.getMessage()); }
        return "redirect:/financeiro/despesas";
    }
}
