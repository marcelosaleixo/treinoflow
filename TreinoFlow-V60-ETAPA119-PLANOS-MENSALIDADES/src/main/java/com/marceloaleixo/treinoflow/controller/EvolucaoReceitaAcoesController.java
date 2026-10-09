package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.InteracaoRecebimentoVinculo;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.repository.InteracaoRecebimentoVinculoRepository;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

/** Etapa 142: histórico mensal de receita atribuída às ações de CRM. */
@Controller
@RequestMapping("/financeiro/evolucao-acoes")
public class EvolucaoReceitaAcoesController {
    private final UsuarioPersonalService personals;
    private final InteracaoRecebimentoVinculoRepository vinculos;

    public EvolucaoReceitaAcoesController(UsuarioPersonalService personals, InteracaoRecebimentoVinculoRepository vinculos) {
        this.personals = personals;
        this.vinculos = vinculos;
    }

    @GetMapping
    public String index(@RequestParam(required = false) Integer ano,
                        @RequestParam(required = false) Integer mes,
                        Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        YearMonth selecionado;
        try {
            selecionado = YearMonth.of(ano == null ? LocalDate.now().getYear() : ano,
                    mes == null ? LocalDate.now().getMonthValue() : mes);
        } catch (RuntimeException ex) {
            selecionado = YearMonth.now();
        }

        List<InteracaoRecebimentoVinculo> todos = vinculos
                .findByInteracaoPersonalIdOrderByDataVinculoDesc(personal.getId()).stream()
                .filter(v -> v.getPagamento() != null && v.getPagamento().getDataPagamento() != null)
                .toList();

        List<Map<String, Object>> meses = new ArrayList<>();
        YearMonth primeiro = selecionado.minusMonths(11);
        BigDecimal totalJanela = BigDecimal.ZERO;
        long recebimentosJanela = 0;
        for (int i = 0; i < 12; i++) {
            YearMonth periodo = primeiro.plusMonths(i);
            final YearMonth p = periodo;
            List<InteracaoRecebimentoVinculo> doMes = todos.stream()
                    .filter(v -> YearMonth.from(v.getPagamento().getDataPagamento()).equals(p)).toList();
            BigDecimal receita = doMes.stream().map(v -> v.getPagamento().getValor() == null ? BigDecimal.ZERO : v.getPagamento().getValor())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long acoes = doMes.stream().filter(v -> v.getInteracao() != null && v.getInteracao().getId() != null)
                    .map(v -> v.getInteracao().getId()).distinct().count();
            Map<String, Object> linha = new LinkedHashMap<>();
            linha.put("periodo", periodo);
            linha.put("rotulo", periodo.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")) + "/" + periodo.getYear());
            linha.put("receita", receita);
            linha.put("recebimentos", doMes.size());
            linha.put("acoes", acoes);
            linha.put("selecionado", periodo.equals(selecionado));
            meses.add(linha);
            totalJanela = totalJanela.add(receita);
            recebimentosJanela += doMes.size();
        }

        BigDecimal maiorReceita = meses.stream().map(m -> (BigDecimal) m.get("receita")).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        for (Map<String, Object> linha : meses) {
            BigDecimal receitaLinha = (BigDecimal) linha.get("receita");
            double largura = maiorReceita.compareTo(BigDecimal.ZERO) == 0 ? 0d
                    : receitaLinha.multiply(BigDecimal.valueOf(100)).divide(maiorReceita, 2, RoundingMode.HALF_UP).doubleValue();
            linha.put("percentualBarra", largura);
        }

        Map<String, Object> atual = meses.stream().filter(m -> Boolean.TRUE.equals(m.get("selecionado"))).findFirst().orElse(meses.get(11));
        BigDecimal receitaAtual = (BigDecimal) atual.get("receita");
        BigDecimal receitaAnterior = (BigDecimal) meses.get(10).get("receita");
        BigDecimal variacao = receitaAtual.subtract(receitaAnterior);
        BigDecimal variacaoPercentual = receitaAnterior.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO : variacao.multiply(BigDecimal.valueOf(100)).divide(receitaAnterior, 2, RoundingMode.HALF_UP);

        model.addAttribute("periodo", selecionado);
        model.addAttribute("meses", meses);
        model.addAttribute("receitaAtual", receitaAtual);
        model.addAttribute("receitaAnterior", receitaAnterior);
        model.addAttribute("variacao", variacao);
        model.addAttribute("variacaoPercentual", variacaoPercentual);
        model.addAttribute("totalJanela", totalJanela);
        model.addAttribute("recebimentosJanela", recebimentosJanela);
        return "financeiro/evolucao-acoes";
    }
}
