package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.dto.ReceitaAlunoFinanceiraView;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.repository.ContaReceberRepository;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/financeiro/por-aluno")
public class ReceitaPorAlunoController {
    private final UsuarioPersonalService personals;
    private final ContaReceberRepository contas;
    public ReceitaPorAlunoController(UsuarioPersonalService personals, ContaReceberRepository contas) {
        this.personals = personals; this.contas = contas;
    }
    @GetMapping
    public String index(Authentication auth, @RequestParam(required = false) Integer ano,
                        @RequestParam(required = false) Integer mes, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(auth.getName());
        YearMonth periodo = ano != null && mes != null && mes >= 1 && mes <= 12
                ? YearMonth.of(ano, mes) : YearMonth.now();
        List<ReceitaAlunoFinanceiraView> alunos = contas.resumoFinanceiroPorAluno(personal.getId(), periodo.atDay(1), periodo.atEndOfMonth());
        BigDecimal recebido = alunos.stream().map(ReceitaAlunoFinanceiraView::getRecebidoNoMes).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal aberto = alunos.stream().map(ReceitaAlunoFinanceiraView::getEmAberto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal atrasado = alunos.stream().map(ReceitaAlunoFinanceiraView::getAtrasado).reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("personal", personal); model.addAttribute("periodo", periodo);
        model.addAttribute("alunosFinanceiro", alunos); model.addAttribute("totalRecebido", recebido);
        model.addAttribute("totalEmAberto", aberto); model.addAttribute("totalAtrasado", atrasado);
        model.addAttribute("quantidadeAlunos", alunos.size());
        return "financeiro/por-aluno";
    }
}
