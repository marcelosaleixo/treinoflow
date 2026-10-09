package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView;
import com.marceloaleixo.treinoflow.entity.ContaReceber;
import com.marceloaleixo.treinoflow.entity.PlanoMensalidade;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.StatusContaReceber;
import com.marceloaleixo.treinoflow.repository.ContaReceberRepository;
import com.marceloaleixo.treinoflow.repository.PlanoMensalidadeRepository;
import com.marceloaleixo.treinoflow.service.ScoreRiscoAlunoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/financeiro/retencao-integrada")
public class RentabilidadeRetencaoController {
    private final UsuarioPersonalService personals;
    private final PlanoMensalidadeRepository planos;
    private final ContaReceberRepository contas;
    private final ScoreRiscoAlunoService risco;

    public RentabilidadeRetencaoController(UsuarioPersonalService personals, PlanoMensalidadeRepository planos,
            ContaReceberRepository contas, ScoreRiscoAlunoService risco) {
        this.personals = personals; this.planos = planos; this.contas = contas; this.risco = risco;
    }

    @GetMapping
    public String index(Authentication auth, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(auth.getName());
        Long personalId = personal.getId();
        Map<Long, BigDecimal> mensalidades = new HashMap<>();
        for (PlanoMensalidade plano : planos.findByAtivoTrueAndPersonalIdOrderByIdAsc(personalId)) {
            if (plano.getAluno() != null && plano.getAluno().getId() != null)
                mensalidades.merge(plano.getAluno().getId(), seguro(plano.getValor()), BigDecimal::add);
        }
        Map<Long, BigDecimal[]> abertos = new HashMap<>();
        for (ContaReceber conta : contas.findByPersonalIdOrderByDataVencimentoAsc(personalId)) {
            if (conta.getAluno() == null || conta.getAluno().getId() == null || conta.getStatus() == StatusContaReceber.CANCELADA || conta.getStatus() == StatusContaReceber.PAGA) continue;
            BigDecimal[] saldo = abertos.computeIfAbsent(conta.getAluno().getId(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (conta.getStatus() == StatusContaReceber.ATRASADA) saldo[1] = saldo[1].add(seguro(conta.getValor()));
            else if (conta.getStatus() == StatusContaReceber.PENDENTE) saldo[0] = saldo[0].add(seguro(conta.getValor()));
        }
        List<LinhaIntegrada> linhas = new ArrayList<>();
        for (ScoreRiscoAlunoView aluno : risco.listar(personalId)) {
            BigDecimal[] saldo = abertos.getOrDefault(aluno.alunoId(), new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal mensalidade = mensalidades.getOrDefault(aluno.alunoId(), BigDecimal.ZERO);
            String prioridade = prioridade(aluno.score(), saldo[1], mensalidade);
            linhas.add(new LinhaIntegrada(aluno.alunoId(), aluno.nome(), aluno.score(), aluno.nivel(), mensalidade,
                    saldo[0], saldo[1], saldo[0].add(saldo[1]), prioridade, aluno.getMotivoPrincipal(), aluno.getAcaoRecomendada()));
        }
        linhas.sort(Comparator.comparingInt(LinhaIntegrada::score).reversed()
                .thenComparing(LinhaIntegrada::atrasado, Comparator.reverseOrder())
                .thenComparing(LinhaIntegrada::mensalidade, Comparator.reverseOrder()));
        BigDecimal mensalidadeTotal = linhas.stream().map(LinhaIntegrada::mensalidade).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal atrasadoTotal = linhas.stream().map(LinhaIntegrada::atrasado).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal abertoTotal = linhas.stream().map(LinhaIntegrada::emAberto).reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("personal", personal); model.addAttribute("linhas", linhas);
        model.addAttribute("mensalidadeTotal", mensalidadeTotal); model.addAttribute("atrasadoTotal", atrasadoTotal);
        model.addAttribute("abertoTotal", abertoTotal);
        model.addAttribute("prioridades", linhas.stream().filter(l -> !"Acompanhamento regular".equals(l.prioridade())).count());
        return "financeiro/retencao-integrada";
    }

    private BigDecimal seguro(BigDecimal valor) { return valor == null ? BigDecimal.ZERO : valor.max(BigDecimal.ZERO); }
    private String prioridade(int score, BigDecimal atrasado, BigDecimal mensalidade) {
        if (score >= 75 && atrasado.signum() > 0) return "Contato prioritário + revisar cobrança";
        if (score >= 75) return "Contato prioritário de retenção";
        if (atrasado.signum() > 0) return "Revisar cobrança em atraso";
        if (score >= 50) return "Contato preventivo";
        if (mensalidade.signum() == 0) return "Conferir plano ativo";
        return "Acompanhamento regular";
    }
    public record LinhaIntegrada(Long alunoId, String nome, int score, String nivel, BigDecimal mensalidade,
            BigDecimal pendente, BigDecimal atrasado, BigDecimal emAberto, String prioridade, String motivo, String acao) { }
}
