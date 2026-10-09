package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.MetaFinanceira;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.StatusContaReceber;
import com.marceloaleixo.treinoflow.repository.ContaReceberRepository;
import com.marceloaleixo.treinoflow.service.MetaFinanceiraService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/financeiro/indicadores")
public class IndicadoresFinanceirosController {
    private final UsuarioPersonalService personals;
    private final MetaFinanceiraService metas;
    private final ContaReceberRepository contas;

    public IndicadoresFinanceirosController(UsuarioPersonalService personals, MetaFinanceiraService metas,
                                            ContaReceberRepository contas) {
        this.personals = personals;
        this.metas = metas;
        this.contas = contas;
    }

    @GetMapping
    public String index(Authentication auth, @RequestParam(required = false) Integer ano,
                        @RequestParam(required = false) Integer mes, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(auth.getName());
        YearMonth periodo = ano != null && mes != null && mes >= 1 && mes <= 12
                ? YearMonth.of(ano, mes) : YearMonth.now();
        YearMonth anterior = periodo.minusMonths(1);
        Long id = personal.getId();
        BigDecimal receita = zero(metas.receitaRealizada(id, periodo));
        BigDecimal receitaAnterior = zero(metas.receitaRealizada(id, anterior));
        BigDecimal despesas = zero(metas.despesasRealizadas(id, periodo));
        BigDecimal despesasAnterior = zero(metas.despesasRealizadas(id, anterior));
        MetaFinanceira meta = metas.buscar(id, periodo);
        BigDecimal metaReceita = zero(meta.getMetaReceita());
        BigDecimal limiteDespesas = zero(meta.getLimiteDespesas());
        BigDecimal resultado = receita.subtract(despesas);
        BigDecimal resultadoAnterior = receitaAnterior.subtract(despesasAnterior);
        int progresso = metaReceita.signum() == 0 ? 0 : receita.multiply(BigDecimal.valueOf(100))
                .divide(metaReceita, 0, RoundingMode.HALF_UP).intValue();
        int variacaoReceita = variacao(receita, receitaAnterior);
        int variacaoDespesas = variacao(despesas, despesasAnterior);
        List<String> insights = new ArrayList<>();
        if (metaReceita.signum() <= 0) insights.add("Defina uma meta de receita para acompanhar o avanço do mês.");
        else if (receita.compareTo(metaReceita) >= 0) insights.add("Meta de receita atingida. Avalie manter uma reserva para os próximos meses.");
        else insights.add("Faltam " + moeda(metaReceita.subtract(receita)) + " para atingir a meta de receita.");
        if (limiteDespesas.signum() > 0 && despesas.compareTo(limiteDespesas) > 0)
            insights.add("Atenção: as despesas pagas ultrapassaram o limite em " + moeda(despesas.subtract(limiteDespesas)) + ".");
        else if (limiteDespesas.signum() > 0)
            insights.add("Restam " + moeda(limiteDespesas.subtract(despesas)) + " até o limite de despesas definido.");
        if (receitaAnterior.signum() > 0) insights.add("A receita variou " + variacaoReceita + "% em relação ao mês anterior.");
        else if (receita.signum() > 0) insights.add("Há receita registrada neste mês; ainda não existe base de comparação de receita para o mês anterior.");
        if (despesas.signum() > 0 && resultado.signum() < 0) insights.add("O resultado realizado está negativo. Revise recebimentos e despesas antes de assumir novos custos.");
        else if (resultado.compareTo(resultadoAnterior) < 0) insights.add("O resultado caiu em relação ao mês anterior; confira os lançamentos para entender a mudança.");
        BigDecimal atrasado = zero(contas.somarPorStatus(id, StatusContaReceber.ATRASADA));
        model.addAttribute("personal", personal);
        model.addAttribute("periodo", periodo);
        model.addAttribute("receita", receita);
        model.addAttribute("receitaAnterior", receitaAnterior);
        model.addAttribute("despesas", despesas);
        model.addAttribute("despesasAnterior", despesasAnterior);
        model.addAttribute("resultado", resultado);
        model.addAttribute("resultadoAnterior", resultadoAnterior);
        model.addAttribute("metaReceita", metaReceita);
        model.addAttribute("limiteDespesas", limiteDespesas);
        model.addAttribute("progresso", progresso);
        model.addAttribute("variacaoReceita", variacaoReceita);
        model.addAttribute("variacaoDespesas", variacaoDespesas);
        model.addAttribute("contasAtrasadas", atrasado);
        model.addAttribute("insights", insights);
        return "financeiro/indicadores";
    }

    private BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private int variacao(BigDecimal atual, BigDecimal anterior) {
        if (anterior == null || anterior.signum() == 0) return atual == null || atual.signum() == 0 ? 0 : 100;
        return atual.subtract(anterior).multiply(BigDecimal.valueOf(100))
                .divide(anterior, 0, RoundingMode.HALF_UP).intValue();
    }
    private String moeda(BigDecimal value) {
        return "R$ " + value.setScale(2, RoundingMode.HALF_UP).toString().replace('.', ',');
    }
}
