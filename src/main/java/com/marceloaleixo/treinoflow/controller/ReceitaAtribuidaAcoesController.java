package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.InteracaoRecebimentoVinculo;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm;
import com.marceloaleixo.treinoflow.repository.InteracaoRecebimentoVinculoRepository;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/** Etapa 140: receita efetivamente atribuída a ações de CRM por meio de vínculos com pagamentos. */
@Controller
@RequestMapping("/financeiro/receita-atribuida-acoes")
public class ReceitaAtribuidaAcoesController {
    private final UsuarioPersonalService personals;
    private final InteracaoRecebimentoVinculoRepository vinculos;

    public ReceitaAtribuidaAcoesController(UsuarioPersonalService personals, InteracaoRecebimentoVinculoRepository vinculos) {
        this.personals = personals;
        this.vinculos = vinculos;
    }

    @GetMapping
    public String index(@RequestParam(required = false) Integer ano,
                        @RequestParam(required = false) Integer mes,
                        Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        YearMonth periodo;
        try {
            periodo = YearMonth.of(ano == null ? LocalDate.now().getYear() : ano,
                    mes == null ? LocalDate.now().getMonthValue() : mes);
        } catch (RuntimeException ex) {
            periodo = YearMonth.now();
        }
        LocalDate inicio = periodo.atDay(1);
        LocalDate fim = periodo.plusMonths(1).atDay(1);

        List<InteracaoRecebimentoVinculo> doPeriodo = vinculos
                .findByInteracaoPersonalIdOrderByDataVinculoDesc(personal.getId()).stream()
                .filter(v -> v.getPagamento() != null && v.getPagamento().getDataPagamento() != null)
                .filter(v -> !v.getPagamento().getDataPagamento().isBefore(inicio) && v.getPagamento().getDataPagamento().isBefore(fim))
                .toList();

        Map<TipoInteracaoCrm, List<InteracaoRecebimentoVinculo>> porTipo = doPeriodo.stream()
                .filter(v -> v.getInteracao() != null && v.getInteracao().getTipo() != null)
                .collect(Collectors.groupingBy(v -> v.getInteracao().getTipo(), () -> new EnumMap<>(TipoInteracaoCrm.class), Collectors.toList()));
        List<Map<String, Object>> linhas = new ArrayList<>();
        for (TipoInteracaoCrm tipo : TipoInteracaoCrm.values()) {
            List<InteracaoRecebimentoVinculo> grupo = porTipo.getOrDefault(tipo, List.of());
            if (grupo.isEmpty()) continue;
            BigDecimal receita = grupo.stream().map(v -> v.getPagamento().getValor() == null ? BigDecimal.ZERO : v.getPagamento().getValor())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long acoes = grupo.stream().map(v -> v.getInteracao().getId()).filter(Objects::nonNull).distinct().count();
            Map<String, Object> linha = new LinkedHashMap<>();
            linha.put("tipo", tipo.getDescricao());
            linha.put("receita", receita);
            linha.put("recebimentos", grupo.size());
            linha.put("acoes", acoes);
            linha.put("ticketMedio", grupo.isEmpty() ? BigDecimal.ZERO : receita.divide(BigDecimal.valueOf(grupo.size()), 2, java.math.RoundingMode.HALF_UP));
            linhas.add(linha);
        }
        BigDecimal receitaTotal = doPeriodo.stream().map(v -> v.getPagamento().getValor() == null ? BigDecimal.ZERO : v.getPagamento().getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long acoesComReceita = doPeriodo.stream().map(v -> v.getInteracao().getId()).filter(Objects::nonNull).distinct().count();
        model.addAttribute("periodo", periodo);
        model.addAttribute("receitaTotal", receitaTotal);
        model.addAttribute("totalRecebimentos", doPeriodo.size());
        model.addAttribute("acoesComReceita", acoesComReceita);
        model.addAttribute("linhas", linhas);
        model.addAttribute("historico", doPeriodo);
        return "financeiro/receita-atribuida-acoes";
    }
}
