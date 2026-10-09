package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.PagamentoContaReceber;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.repository.InteracaoRecebimentoVinculoRepository;
import com.marceloaleixo.treinoflow.repository.PagamentoContaReceberRepository;
import com.marceloaleixo.treinoflow.service.InteracaoRecebimentoVinculoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/financeiro/vinculos-recebimentos")
public class InteracaoRecebimentoVinculoController {
    private final UsuarioPersonalService personals;
    private final InteracaoCrmRepository interacoes;
    private final PagamentoContaReceberRepository pagamentos;
    private final InteracaoRecebimentoVinculoRepository vinculos;
    private final InteracaoRecebimentoVinculoService service;

    public InteracaoRecebimentoVinculoController(UsuarioPersonalService personals, InteracaoCrmRepository interacoes, PagamentoContaReceberRepository pagamentos, InteracaoRecebimentoVinculoRepository vinculos, InteracaoRecebimentoVinculoService service) {
        this.personals = personals; this.interacoes = interacoes; this.pagamentos = pagamentos; this.vinculos = vinculos; this.service = service;
    }

    @GetMapping
    public String index(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        List<InteracaoCrm> acoes = interacoes.findByPersonalIdOrderByDataContatoDesc(personal.getId());
        List<PagamentoContaReceber> recebimentos = pagamentos.listarPeriodo(personal.getId(), LocalDate.now().minusYears(2), LocalDate.now().plusDays(1));
        model.addAttribute("acoes", acoes);
        model.addAttribute("recebimentos", recebimentos);
        model.addAttribute("vinculos", vinculos.findByInteracaoPersonalIdOrderByDataVinculoDesc(personal.getId()));
        model.addAttribute("pagamentosAtribuidos", vinculos.findByInteracaoPersonalIdOrderByDataVinculoDesc(personal.getId()).stream().map(v -> v.getPagamento().getId()).collect(java.util.stream.Collectors.toSet()));
        return "financeiro/vinculos-recebimentos";
    }

    @PostMapping("/vincular")
    public String vincular(@RequestParam Long interacaoId, @RequestParam Long pagamentoId, @RequestParam(required = false) String observacao, Authentication authentication, RedirectAttributes redirect) {
        try {
            UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
            service.vincular(interacaoId, pagamentoId, personal.getId(), observacao);
            redirect.addFlashAttribute("sucesso", "Recebimento vinculado à ação com sucesso.");
        } catch (IllegalArgumentException ex) { redirect.addFlashAttribute("erro", ex.getMessage()); }
        return "redirect:/financeiro/vinculos-recebimentos";
    }

    @PostMapping("/excluir")
    public String excluir(@RequestParam Long vinculoId, Authentication authentication, RedirectAttributes redirect) {
        try {
            UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
            service.excluir(vinculoId, personal.getId());
            redirect.addFlashAttribute("sucesso", "Vínculo removido. O recebimento financeiro original foi preservado.");
        } catch (IllegalArgumentException ex) { redirect.addFlashAttribute("erro", ex.getMessage()); }
        return "redirect:/financeiro/vinculos-recebimentos";
    }
}
