package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.InteracaoRecebimentoVinculo;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.repository.InteracaoRecebimentoVinculoRepository;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/** Etapa 141: analisa conversão e receita atribuída às ações de CRM do período. */
@Controller
@RequestMapping("/financeiro/conversao-acoes")
public class ConversaoAcoesFinanceirasController {
    private final UsuarioPersonalService personals;
    private final InteracaoCrmRepository interacoes;
    private final InteracaoRecebimentoVinculoRepository vinculos;

    public ConversaoAcoesFinanceirasController(UsuarioPersonalService personals,
            InteracaoCrmRepository interacoes, InteracaoRecebimentoVinculoRepository vinculos) {
        this.personals = personals;
        this.interacoes = interacoes;
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
        LocalDateTime inicio = periodo.atDay(1).atStartOfDay();
        LocalDateTime fim = periodo.plusMonths(1).atDay(1).atStartOfDay();
        List<InteracaoCrm> acoes = interacoes.findByPersonalIdOrderByDataContatoDesc(personal.getId()).stream()
                .filter(i -> i.getDataContato() != null && !i.getDataContato().isBefore(inicio) && i.getDataContato().isBefore(fim))
                .toList();
        Set<Long> idsAcoes = acoes.stream().map(InteracaoCrm::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<InteracaoRecebimentoVinculo> recebimentos = vinculos.findByInteracaoPersonalIdOrderByDataVinculoDesc(personal.getId()).stream()
                .filter(v -> v.getInteracao() != null && idsAcoes.contains(v.getInteracao().getId()))
                .filter(v -> v.getPagamento() != null && v.getPagamento().getValor() != null)
                .toList();
        Map<Long, BigDecimal> receitaPorAcao = recebimentos.stream().collect(Collectors.groupingBy(
                v -> v.getInteracao().getId(), Collectors.mapping(v -> v.getPagamento().getValor(),
                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        List<Map<String, Object>> linhas = new ArrayList<>();
        for (TipoInteracaoCrm tipo : TipoInteracaoCrm.values()) {
            List<InteracaoCrm> grupo = acoes.stream().filter(i -> i.getTipo() == tipo).toList();
            if (grupo.isEmpty()) continue;
            long renovados = contar(grupo, ResultadoCrm.RENOVADO);
            long recuperados = contar(grupo, ResultadoCrm.RECUPERADO);
            long cancelamentos = contar(grupo, ResultadoCrm.CANCELAMENTO);
            long encerrados = renovados + recuperados + cancelamentos;
            BigDecimal receita = grupo.stream().map(i -> receitaPorAcao.getOrDefault(i.getId(), BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long comReceita = grupo.stream().filter(i -> receitaPorAcao.getOrDefault(i.getId(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0).count();
            Map<String, Object> linha = new LinkedHashMap<>();
            linha.put("tipo", tipo.getDescricao());
            linha.put("acoes", grupo.size());
            linha.put("alunos", grupo.stream().map(i -> i.getAluno() == null ? null : i.getAluno().getId()).filter(Objects::nonNull).distinct().count());
            linha.put("recuperados", recuperados);
            linha.put("renovados", renovados);
            linha.put("cancelamentos", cancelamentos);
            linha.put("encerrados", encerrados);
            linha.put("taxaConversao", encerrados == 0 ? null : Math.round((recuperados + renovados) * 1000.0 / encerrados) / 10.0);
            linha.put("receita", receita);
            linha.put("acoesComReceita", comReceita);
            linhas.add(linha);
        }
        long recuperados = contar(acoes, ResultadoCrm.RECUPERADO);
        long renovados = contar(acoes, ResultadoCrm.RENOVADO);
        long cancelamentos = contar(acoes, ResultadoCrm.CANCELAMENTO);
        long encerrados = recuperados + renovados + cancelamentos;
        BigDecimal receitaTotal = recebimentos.stream().map(v -> v.getPagamento().getValor()).reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("periodo", periodo);
        model.addAttribute("totalAcoes", acoes.size());
        model.addAttribute("alunosAlcancados", acoes.stream().map(i -> i.getAluno() == null ? null : i.getAluno().getId()).filter(Objects::nonNull).distinct().count());
        model.addAttribute("totalEncerrados", encerrados);
        model.addAttribute("taxaConversaoGeral", encerrados == 0 ? null : Math.round((recuperados + renovados) * 1000.0 / encerrados) / 10.0);
        model.addAttribute("receitaTotal", receitaTotal);
        model.addAttribute("linhas", linhas);
        model.addAttribute("historico", acoes.stream().limit(50).toList());
        return "financeiro/conversao-acoes";
    }

    private long contar(List<InteracaoCrm> lista, ResultadoCrm resultado) {
        return lista.stream().filter(i -> i.getResultado() == resultado).count();
    }
}
