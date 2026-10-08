package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.PlanoMensalidadeService;
import com.marceloaleixo.treinoflow.service.RecorrenciaCobrancaService;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/financeiro/mensalidades")
public class PlanoMensalidadeController {
    private final PlanoMensalidadeService planos;
    private final AlunoRepository alunos;
    private final com.marceloaleixo.treinoflow.service.UsuarioPersonalService personals;
    private final RecorrenciaCobrancaService recorrencia;

    public PlanoMensalidadeController(PlanoMensalidadeService planos, AlunoRepository alunos,
                                      com.marceloaleixo.treinoflow.service.UsuarioPersonalService personals,
                                      RecorrenciaCobrancaService recorrencia) {
        this.planos = planos; this.alunos = alunos; this.personals = personals; this.recorrencia = recorrencia;
    }

    @GetMapping
    public String index(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("planos", planos.listar(personal.getId()));
        return "financeiro/mensalidades";
    }

    @GetMapping("/nova")
    public String nova(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("alunos", alunos.findByPersonalIdAndStatusOrderByNomeAsc(personal.getId(), "ATIVO"));
        model.addAttribute("hoje", LocalDate.now());
        return "financeiro/mensalidade-form";
    }

    @PostMapping
    public String salvar(Authentication authentication, @RequestParam Long alunoId, @RequestParam String nome,
                         @RequestParam BigDecimal valor, @RequestParam Integer diaVencimento,
                         @RequestParam LocalDate dataInicio, @RequestParam(required=false) String observacao,
                         RedirectAttributes redirect) {
        try {
            UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
            planos.criar(personal.getId(), alunoId, nome, valor, diaVencimento, dataInicio, observacao);
            redirect.addFlashAttribute("sucesso", "Plano mensal criado com sucesso.");
        } catch (Exception ex) { redirect.addFlashAttribute("erro", ex.getMessage()); }
        return "redirect:/financeiro/mensalidades";
    }

    @PostMapping("/{id}/alternar")
    public String alternar(Authentication authentication, @PathVariable Long id, RedirectAttributes redirect) {
        try {
            UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
            var plano = planos.alternarAtivo(personal.getId(), id);
            redirect.addFlashAttribute("sucesso", plano.isAtivo() ? "Plano ativado." : "Plano pausado.");
        } catch (Exception ex) { redirect.addFlashAttribute("erro", ex.getMessage()); }
        return "redirect:/financeiro/mensalidades";
    }

    @PostMapping("/{id}/gerar-cobranca")
    public String gerarCobranca(Authentication authentication, @PathVariable Long id, RedirectAttributes redirect) {
        try {
            UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
            planos.gerarCobranca(personal.getId(), id, LocalDate.now());
            redirect.addFlashAttribute("sucesso", "Cobrança mensal gerada e enviada para Contas a Receber.");
        } catch (Exception ex) { redirect.addFlashAttribute("erro", ex.getMessage()); }
        return "redirect:/financeiro/mensalidades";
    }

    /** Processamento manual restrito ao Personal autenticado, sem alterar planos de outros usuários. */
    @PostMapping("/processar-recorrencia")
    public String processarRecorrencia(Authentication authentication, RedirectAttributes redirect) {
        try {
            UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
            int criadas = recorrencia.processarParaPersonal(personal.getId(), LocalDate.now());
            redirect.addFlashAttribute("sucesso", criadas == 0
                    ? "Nenhuma nova cobrança mensal precisava ser gerada."
                    : criadas + " cobrança(s) mensal(is) gerada(s) automaticamente.");
        } catch (Exception ex) { redirect.addFlashAttribute("erro", ex.getMessage()); }
        return "redirect:/financeiro/mensalidades";
    }
}
