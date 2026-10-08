package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.enums.CanalCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm;
import com.marceloaleixo.treinoflow.service.CrmService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import com.marceloaleixo.treinoflow.service.PlanoAcaoInteligenteService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PlanoAcaoInteligenteController {
    private final UsuarioPersonalService personals;
    private final PlanoAcaoInteligenteService plano;
    private final CrmService crm;

    public PlanoAcaoInteligenteController(UsuarioPersonalService personals,
                                          PlanoAcaoInteligenteService plano,
                                          CrmService crm) {
        this.personals = personals;
        this.plano = plano;
        this.crm = crm;
    }

    @GetMapping("/performance/plano-inteligente")
    public String plano(Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        var acoes = plano.listar(personal.getId());
        model.addAttribute("personal", personal);
        model.addAttribute("acoes", acoes);
        model.addAttribute("atencao", acoes.stream().filter(a -> "ATENÇÃO".equals(a.prioridade())).count());
        model.addAttribute("alta", acoes.stream().filter(a -> "ALTA".equals(a.prioridade())).count());
        model.addAttribute("media", acoes.stream().filter(a -> "MÉDIA".equals(a.prioridade())).count());
        return "performance/plano-inteligente";
    }
    @PostMapping("/performance/plano-inteligente/{alunoId}/executar")
    public String executar(@PathVariable Long alunoId,
                           @RequestParam CanalCrm canal,
                           @RequestParam(defaultValue = "EM_ACOMPANHAMENTO") ResultadoCrm resultado,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataProximaAcao,
                           @RequestParam(required = false) String observacao,
                           Authentication authentication,
                           RedirectAttributes ra) {
        try {
            UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
            var acao = plano.listar(personal.getId()).stream()
                    .filter(a -> a.alunoId().equals(alunoId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Ação inteligente não encontrada para este aluno."));

            InteracaoCrm interacao = new InteracaoCrm();
            interacao.setCanal(canal);
            interacao.setTipo(("ATENÇÃO".equals(acao.prioridade()) || "ALTA".equals(acao.prioridade()))
                    ? TipoInteracaoCrm.RETENCAO : TipoInteracaoCrm.CONTATO);
            interacao.setResultado(resultado);
            interacao.setAssunto((acao.acaoTitulo() == null ? "Ação inteligente" : acao.acaoTitulo()).substring(0, Math.min(160, (acao.acaoTitulo() == null ? "Ação inteligente" : acao.acaoTitulo()).length())));
            String extra = observacao == null || observacao.isBlank() ? "" : "\n\nObservação do Personal: " + observacao.trim();
            interacao.setDescricao("Ação recomendada pelo TreinoFlow: " + acao.acaoDetalhada()
                    + "\n\nMotivo identificado: " + acao.motivoPrincipal()
                    + "\n\nMensagem sugerida: " + acao.mensagemWhatsApp() + extra);
            interacao.setDataProximaAcao(dataProximaAcao);
            crm.salvar(personal.getId(), alunoId, interacao);
            ra.addFlashAttribute("sucesso", "Ação registrada no CRM com sucesso.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/performance/plano-inteligente";
    }

}
