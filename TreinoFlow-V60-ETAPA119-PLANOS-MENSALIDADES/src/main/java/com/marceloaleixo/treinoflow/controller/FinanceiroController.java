package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.FormaPagamento;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.service.ContaReceberService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/financeiro")
public class FinanceiroController {
    private final UsuarioPersonalService personals;
    private final ContaReceberService contas;
    private final AlunoRepository alunos;

    public FinanceiroController(UsuarioPersonalService personals, ContaReceberService contas, AlunoRepository alunos) {
        this.personals = personals;
        this.contas = contas;
        this.alunos = alunos;
    }

    @GetMapping
    public String dashboard(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        Long personalId = personal.getId();
        contas.atualizarAtrasadas(personalId);
        model.addAttribute("personal", personal);
        model.addAttribute("recebidoNoMes", contas.recebidoNoMes(personalId));
        model.addAttribute("previstoNoMes", contas.previstoNoMes(personalId));
        model.addAttribute("aReceber", contas.aReceber(personalId));
        model.addAttribute("emAtraso", contas.emAtraso(personalId));
        model.addAttribute("quantidadeAtrasadas", contas.quantidadeAtrasadas(personalId));
        model.addAttribute("contas", contas.proximas(personalId));
        return "financeiro/index";
    }

    @GetMapping("/contas-receber/nova")
    public String nova(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("alunos", alunos.findByPersonalIdAndStatusOrderByNomeAsc(personal.getId(), "ATIVO"));
        model.addAttribute("hoje", LocalDate.now());
        return "financeiro/conta-form";
    }

    @PostMapping("/contas-receber")
    public String salvar(Authentication authentication,
                         @RequestParam(required = false) Long alunoId,
                         @RequestParam String descricao,
                         @RequestParam BigDecimal valor,
                         @RequestParam LocalDate dataVencimento,
                         @RequestParam(required = false) String observacao,
                         org.springframework.web.servlet.mvc.support.RedirectAttributes redirect) {
        try {
            UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
            contas.criar(personal.getId(), alunoId, descricao, valor, dataVencimento, observacao);
            redirect.addFlashAttribute("sucesso", "Conta a receber cadastrada com sucesso.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/financeiro";
    }

    @PostMapping("/contas-receber/{id}/pagar")
    public String pagar(Authentication authentication, @PathVariable Long id,
                        @RequestParam(name = "formaPagamento") List<FormaPagamento> formasPagamento,
                        @RequestParam(name = "valorPagamento") List<BigDecimal> valoresPagamento,
                        @RequestParam(required = false) LocalDate dataPagamento,
                        org.springframework.web.servlet.mvc.support.RedirectAttributes redirect) {
        try {
            UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
            contas.marcarPaga(personal.getId(), id, formasPagamento, valoresPagamento, dataPagamento);
            redirect.addFlashAttribute("sucesso", "Pagamento registrado com sucesso.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/financeiro";
    }

    @PostMapping("/contas-receber/{id}/cancelar")
    public String cancelar(Authentication authentication, @PathVariable Long id,
                           org.springframework.web.servlet.mvc.support.RedirectAttributes redirect) {
        try {
            UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
            contas.cancelar(personal.getId(), id);
            redirect.addFlashAttribute("sucesso", "Conta cancelada.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/financeiro";
    }
}
