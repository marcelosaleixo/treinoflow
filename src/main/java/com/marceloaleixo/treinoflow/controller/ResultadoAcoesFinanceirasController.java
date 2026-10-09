package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/** Etapa 136: indicadores descritivos dos resultados registrados nas ações financeiras/CRM. */
@Controller
@RequestMapping("/financeiro/resultado-acoes")
public class ResultadoAcoesFinanceirasController {
    private final UsuarioPersonalService personals;
    private final InteracaoCrmRepository interacoes;

    public ResultadoAcoesFinanceirasController(UsuarioPersonalService personals, InteracaoCrmRepository interacoes) {
        this.personals = personals;
        this.interacoes = interacoes;
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
        LocalDateTime inicio = periodo.atDay(1).atStartOfDay();
        LocalDateTime fim = periodo.plusMonths(1).atDay(1).atStartOfDay();
        List<InteracaoCrm> todas = interacoes.findByPersonalIdOrderByDataContatoDesc(personal.getId());
        List<InteracaoCrm> doPeriodo = todas.stream()
                .filter(i -> i.getDataContato() != null && !i.getDataContato().isBefore(inicio) && i.getDataContato().isBefore(fim))
                .toList();
        long recuperados = contar(doPeriodo, ResultadoCrm.RECUPERADO);
        long renovados = contar(doPeriodo, ResultadoCrm.RENOVADO);
        long cancelamentos = contar(doPeriodo, ResultadoCrm.CANCELAMENTO);
        long semResposta = contar(doPeriodo, ResultadoCrm.SEM_RESPOSTA);
        long emAcompanhamento = contar(doPeriodo, ResultadoCrm.EM_ACOMPANHAMENTO);
        long cobrancas = doPeriodo.stream().filter(i -> i.getTipo() == TipoInteracaoCrm.COBRANCA).count();
        long retencoes = doPeriodo.stream().filter(i -> i.getTipo() == TipoInteracaoCrm.RETENCAO).count();
        long resolvidas = recuperados + renovados + cancelamentos;
        double taxaResultadoPositivo = resolvidas == 0 ? 0.0 : (recuperados + renovados) * 100.0 / resolvidas;
        model.addAttribute("periodo", periodo);
        model.addAttribute("totalAcoes", doPeriodo.size());
        model.addAttribute("recuperados", recuperados);
        model.addAttribute("renovados", renovados);
        model.addAttribute("cancelamentos", cancelamentos);
        model.addAttribute("semResposta", semResposta);
        model.addAttribute("emAcompanhamento", emAcompanhamento);
        model.addAttribute("cobrancas", cobrancas);
        model.addAttribute("retencoes", retencoes);
        model.addAttribute("resolvidas", resolvidas);
        model.addAttribute("taxaResultadoPositivo", Math.round(taxaResultadoPositivo * 10.0) / 10.0);
        model.addAttribute("acoes", doPeriodo.stream().limit(100).toList());
        return "financeiro/resultado-acoes";
    }

    private long contar(List<InteracaoCrm> lista, ResultadoCrm resultado) {
        return lista.stream().filter(i -> i.getResultado() == resultado).count();
    }
}
