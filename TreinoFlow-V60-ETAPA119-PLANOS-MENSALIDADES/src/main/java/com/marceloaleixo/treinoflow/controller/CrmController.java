package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.CanalCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm;
import com.marceloaleixo.treinoflow.service.CrmService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/crm")
public class CrmController {
    private final CrmService crm;
    private final UsuarioPersonalService usuarioPersonalService;

    public CrmController(CrmService crm, UsuarioPersonalService usuarioPersonalService) {
        this.crm = crm;
        this.usuarioPersonalService = usuarioPersonalService;
    }

    @GetMapping
    public String dashboard(Authentication authentication, Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "8") int size,
                            @RequestParam(required = false) String q) {
        UsuarioPersonal personal = personal(authentication);
        var pagina = crm.listarAlunos(personal.getId(), q, PageRequest.of(Math.max(0, page), Math.min(50, Math.max(5, size))));
        model.addAttribute("personal", personal);
        model.addAttribute("pagina", pagina);
        model.addAttribute("q", q == null ? "" : q.trim());
        model.addAttribute("dados", crm.dadosDashboard(personal.getId()));
        model.addAttribute("acoes", crm.acoesPendentes(personal.getId()));
        return "crm/index";
    }

    @GetMapping("/alunos/{id}")
    public String aluno(@PathVariable Long id,
                        Authentication authentication,
                        Model model,
                        @RequestParam(required = false) String acao,
                        @RequestParam(required = false) String motivo,
                        @RequestParam(required = false) String nivel) {
        UsuarioPersonal personal = personal(authentication);
        Aluno aluno = crm.buscarAluno(personal.getId(), id);

        InteracaoCrm interacao = new InteracaoCrm();
        boolean acaoRetencao = "RETENCAO".equalsIgnoreCase(acao);
        if (acaoRetencao) {
            interacao.setTipo(TipoInteracaoCrm.RETENCAO);
            interacao.setResultado(ResultadoCrm.EM_ACOMPANHAMENTO);
            interacao.setCanal(aluno.getTelefone() != null && !aluno.getTelefone().isBlank()
                    ? CanalCrm.WHATSAPP : CanalCrm.TELEFONE);
            interacao.setAssunto("Ação de retenção — acompanhamento " +
                    (nivel == null || nivel.isBlank() ? "preventivo" : nivel.toLowerCase()));
            String sinal = motivo == null || motivo.isBlank()
                    ? "Sinal de risco identificado no painel Atenção hoje."
                    : motivo.trim();
            interacao.setDescricao("Motivo identificado pelo TreinoFlow: " + sinal
                    + "\n\nRegistrar o contato realizado, a resposta do aluno e o próximo passo.");
            interacao.setDataProximaAcao(LocalDate.now().plusDays(3));
            model.addAttribute("acaoRetencao", true);
        } else {
            model.addAttribute("acaoRetencao", false);
        }

        model.addAttribute("personal", personal);
        model.addAttribute("aluno", aluno);
        model.addAttribute("timeline", crm.timeline(personal.getId(), id));
        model.addAttribute("interacao", interacao);
        model.addAttribute("canais", CanalCrm.values());
        model.addAttribute("tipos", TipoInteracaoCrm.values());
        model.addAttribute("resultados", ResultadoCrm.values());
        return "crm/aluno";
    }

    @PostMapping("/alunos/{id}/interacoes/salvar")
    public String salvar(@PathVariable Long id, @ModelAttribute("interacao") InteracaoCrm interacao,
                         Authentication authentication, RedirectAttributes ra) {
        try {
            UsuarioPersonal personal = personal(authentication);
            crm.salvar(personal.getId(), id, interacao);
            ra.addFlashAttribute("sucesso", "Interação registrada com sucesso.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/crm/alunos/" + id;
    }

    private UsuarioPersonal personal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) throw new IllegalArgumentException("Usuário não autenticado.");
        return usuarioPersonalService.buscarPorEmail(authentication.getName());
    }
}
