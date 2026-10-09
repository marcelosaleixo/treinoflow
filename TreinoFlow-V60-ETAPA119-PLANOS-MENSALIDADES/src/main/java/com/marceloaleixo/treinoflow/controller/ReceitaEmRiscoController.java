package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.dto.ReceitaEmRiscoView;
import com.marceloaleixo.treinoflow.entity.ContaReceber;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.StatusContaReceber;
import com.marceloaleixo.treinoflow.repository.ContaReceberRepository;
import com.marceloaleixo.treinoflow.service.ScoreRiscoAlunoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/financeiro/receita-em-risco")
public class ReceitaEmRiscoController {
    private final UsuarioPersonalService personals;
    private final ContaReceberRepository contas;
    private final ScoreRiscoAlunoService riscoService;

    public ReceitaEmRiscoController(UsuarioPersonalService personals,
                                    ContaReceberRepository contas,
                                    ScoreRiscoAlunoService riscoService) {
        this.personals = personals;
        this.contas = contas;
        this.riscoService = riscoService;
    }

    @GetMapping
    public String index(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        Long personalId = personal.getId();

        Map<Long, BigDecimal[]> saldos = new LinkedHashMap<>();
        for (ContaReceber conta : contas.findByPersonalIdOrderByDataVencimentoAsc(personalId)) {
            if (conta.getAluno() == null || conta.getAluno().getId() == null || conta.getStatus() == StatusContaReceber.CANCELADA || conta.getStatus() == StatusContaReceber.PAGA) {
                continue;
            }
            Long alunoId = conta.getAluno().getId();
            BigDecimal[] valores = saldos.computeIfAbsent(alunoId, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (conta.getStatus() == StatusContaReceber.ATRASADA) {
                valores[1] = valores[1].add(valorSeguro(conta.getValor()));
            } else if (conta.getStatus() == StatusContaReceber.PENDENTE) {
                valores[0] = valores[0].add(valorSeguro(conta.getValor()));
            }
        }

        List<ReceitaEmRiscoView> linhas = new ArrayList<>();
        for (var aluno : riscoService.listar(personalId)) {
            BigDecimal[] valores = saldos.getOrDefault(aluno.alunoId(), new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal exposicao = valores[0].add(valores[1]);
            linhas.add(new ReceitaEmRiscoView(aluno.alunoId(), aluno.nome(), aluno.score(), aluno.nivel(),
                    valores[0], valores[1], exposicao, aluno.acaoRecomendada(), aluno.getMotivoPrincipal()));
        }
        linhas.sort(Comparator.comparingInt(ReceitaEmRiscoView::scoreRetencao).reversed()
                .thenComparing(ReceitaEmRiscoView::exposicaoEmAberto, Comparator.reverseOrder())
                .thenComparing(ReceitaEmRiscoView::nome, String.CASE_INSENSITIVE_ORDER));

        BigDecimal pendente = linhas.stream().map(ReceitaEmRiscoView::valorPendente).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal atrasado = linhas.stream().map(ReceitaEmRiscoView::valorAtrasado).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal exposicao = pendente.add(atrasado);
        model.addAttribute("personal", personal);
        model.addAttribute("linhas", linhas);
        model.addAttribute("totalPendente", pendente);
        model.addAttribute("totalAtrasado", atrasado);
        model.addAttribute("exposicaoTotal", exposicao);
        model.addAttribute("alunosComExposicao", linhas.stream().filter(l -> l.exposicaoEmAberto().compareTo(BigDecimal.ZERO) > 0).count());
        model.addAttribute("altoRiscoComExposicao", linhas.stream().filter(l -> l.scoreRetencao() >= 50 && l.exposicaoEmAberto().compareTo(BigDecimal.ZERO) > 0).count());
        return "financeiro/receita-em-risco";
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor.max(BigDecimal.ZERO);
    }
}
