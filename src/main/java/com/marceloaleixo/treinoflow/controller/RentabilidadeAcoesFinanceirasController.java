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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Etapa 137: comparação descritiva dos resultados de CRM por tipo de ação. */
@Controller
@RequestMapping("/financeiro/rentabilidade-acoes")
public class RentabilidadeAcoesFinanceirasController {
    private final UsuarioPersonalService personals;
    private final InteracaoCrmRepository interacoes;

    public RentabilidadeAcoesFinanceirasController(UsuarioPersonalService personals, InteracaoCrmRepository interacoes) {
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
        List<InteracaoCrm> doPeriodo = interacoes.findByPersonalIdOrderByDataContatoDesc(personal.getId()).stream()
                .filter(i -> i.getDataContato() != null && !i.getDataContato().isBefore(inicio) && i.getDataContato().isBefore(fim))
                .toList();

        List<Map<String, Object>> linhas = new ArrayList<>();
        for (TipoInteracaoCrm tipo : TipoInteracaoCrm.values()) {
            List<InteracaoCrm> grupo = doPeriodo.stream().filter(i -> i.getTipo() == tipo).toList();
            if (grupo.isEmpty()) continue;
            long recuperados = contar(grupo, ResultadoCrm.RECUPERADO);
            long renovados = contar(grupo, ResultadoCrm.RENOVADO);
            long cancelamentos = contar(grupo, ResultadoCrm.CANCELAMENTO);
            long encerrados = recuperados + renovados + cancelamentos;
            Map<String, Object> linha = new LinkedHashMap<>();
            linha.put("tipo", tipo.getDescricao());
            linha.put("total", grupo.size());
            linha.put("recuperados", recuperados);
            linha.put("renovados", renovados);
            linha.put("cancelamentos", cancelamentos);
            linha.put("emAcompanhamento", contar(grupo, ResultadoCrm.EM_ACOMPANHAMENTO));
            linha.put("semResposta", contar(grupo, ResultadoCrm.SEM_RESPOSTA));
            linha.put("taxaPositiva", encerrados == 0 ? null : Math.round((recuperados + renovados) * 1000.0 / encerrados) / 10.0);
            linhas.add(linha);
        }
        long encerrados = doPeriodo.stream().filter(i -> i.getResultado() == ResultadoCrm.RECUPERADO || i.getResultado() == ResultadoCrm.RENOVADO || i.getResultado() == ResultadoCrm.CANCELAMENTO).count();
        long positivos = doPeriodo.stream().filter(i -> i.getResultado() == ResultadoCrm.RECUPERADO || i.getResultado() == ResultadoCrm.RENOVADO).count();
        model.addAttribute("periodo", periodo);
        model.addAttribute("totalAcoes", doPeriodo.size());
        model.addAttribute("tiposComAcoes", linhas.size());
        model.addAttribute("taxaPositivaGeral", encerrados == 0 ? null : Math.round(positivos * 1000.0 / encerrados) / 10.0);
        model.addAttribute("linhas", linhas);
        model.addAttribute("historico", doPeriodo.stream().limit(50).toList());
        return "financeiro/rentabilidade-acoes";
    }

    private long contar(List<InteracaoCrm> lista, ResultadoCrm resultado) {
        return lista.stream().filter(i -> i.getResultado() == resultado).count();
    }
}
