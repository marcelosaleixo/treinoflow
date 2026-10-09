package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.PrevisaoFinanceiraService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.marceloaleixo.treinoflow.dto.PrevisaoFinanceiraMesView;
import com.marceloaleixo.treinoflow.service.RelatorioCsvService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/financeiro/previsao")
public class PrevisaoFinanceiraController {
    private final UsuarioPersonalService personals;
    private final PrevisaoFinanceiraService previsao;
    private final RelatorioCsvService csv;
    public PrevisaoFinanceiraController(UsuarioPersonalService personals, PrevisaoFinanceiraService previsao, RelatorioCsvService csv) {
        this.personals = personals; this.previsao = previsao; this.csv = csv;
    }
    @GetMapping
    public String index(Authentication authentication, @RequestParam(defaultValue = "3") int meses, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        int horizonte = Math.max(1, Math.min(12, meses));
        model.addAttribute("personal", personal);
        model.addAttribute("meses", horizonte);
        model.addAttribute("previsoes", previsao.projetar(personal.getId(), horizonte));
        return "financeiro/previsao";
    }
    @GetMapping(value = "/exportar", produces = "text/csv")
    public ResponseEntity<byte[]> exportar(Authentication authentication, @RequestParam(defaultValue = "3") int meses) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        int horizonte = Math.max(1, Math.min(12, meses));
        StringBuilder conteudo = new StringBuilder();
        conteudo.append(csv.linha("Mês", "Receita prevista (R$)", "Despesas pendentes (R$)", "Saldo projetado (R$)", "Observação"));
        for (PrevisaoFinanceiraMesView item : previsao.projetar(personal.getId(), horizonte)) {
            conteudo.append(csv.linha(csv.mes(item.periodo()), csv.dinheiro(item.receitaPrevista()),
                    csv.dinheiro(item.despesasCadastradas()), csv.dinheiro(item.saldoProjetado()), item.observacao()));
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=treinoflow-previsao-financeira.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8")).body(csv.bytes(conteudo.toString()));
    }
}
