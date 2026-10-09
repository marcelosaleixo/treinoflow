package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.StatusContaReceber;
import com.marceloaleixo.treinoflow.repository.ContaReceberRepository;
import com.marceloaleixo.treinoflow.service.DespesaPersonalService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.marceloaleixo.treinoflow.service.RelatorioCsvService;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Controller
@RequestMapping("/financeiro/dre")
public class DreFinanceiroController {
    private final UsuarioPersonalService personals;
    private final ContaReceberRepository contas;
    private final DespesaPersonalService despesas;
    private final RelatorioCsvService csv;
    public DreFinanceiroController(UsuarioPersonalService personals, ContaReceberRepository contas, DespesaPersonalService despesas, RelatorioCsvService csv) {
        this.personals = personals; this.contas = contas; this.despesas = despesas; this.csv = csv;
    }
    @GetMapping
    public String index(Authentication auth, @RequestParam(required = false) Integer ano,
                        @RequestParam(required = false) Integer mes, Model model) {
        UsuarioPersonal p = personals.buscarPorEmail(auth.getName());
        YearMonth periodo = (ano != null && mes != null && mes >= 1 && mes <= 12) ? YearMonth.of(ano, mes) : YearMonth.now();
        LocalDate inicio = periodo.atDay(1), fim = periodo.atEndOfMonth();
        BigDecimal receita = contas.somarPagasNoPeriodo(p.getId(), StatusContaReceber.PAGA, inicio, fim);
        BigDecimal despesasPagas = despesas.totalPago(p.getId(), inicio, fim);
        BigDecimal despesasPendentes = despesas.totalPendente(p.getId(), inicio, fim);
        model.addAttribute("personal", p); model.addAttribute("periodo", periodo);
        model.addAttribute("receita", receita); model.addAttribute("despesasPagas", despesasPagas);
        model.addAttribute("despesasPendentes", despesasPendentes);
        model.addAttribute("resultado", receita.subtract(despesasPagas));
        return "financeiro/dre";
    }
    @GetMapping(value = "/exportar", produces = "text/csv")
    public ResponseEntity<byte[]> exportar(Authentication auth, @RequestParam(required = false) Integer ano,
                                           @RequestParam(required = false) Integer mes) {
        UsuarioPersonal p = personals.buscarPorEmail(auth.getName());
        YearMonth periodo = (ano != null && mes != null && mes >= 1 && mes <= 12)
                ? YearMonth.of(ano, mes) : YearMonth.now();
        LocalDate inicio = periodo.atDay(1), fim = periodo.atEndOfMonth();
        BigDecimal receita = contas.somarPagasNoPeriodo(p.getId(), StatusContaReceber.PAGA, inicio, fim);
        BigDecimal despesasPagas = despesas.totalPago(p.getId(), inicio, fim);
        BigDecimal despesasPendentes = despesas.totalPendente(p.getId(), inicio, fim);
        StringBuilder conteudo = new StringBuilder();
        conteudo.append(csv.linha("Período", "Receitas recebidas (R$)", "Despesas pagas (R$)",
                "Resultado simplificado (R$)", "Despesas pendentes (R$)"));
        conteudo.append(csv.linha(csv.mes(periodo), csv.dinheiro(receita), csv.dinheiro(despesasPagas),
                csv.dinheiro(receita.subtract(despesasPagas)), csv.dinheiro(despesasPendentes)));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=treinoflow-dre-" + periodo + ".csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8")).body(csv.bytes(conteudo.toString()));
    }
}
