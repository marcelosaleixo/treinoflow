package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.CanalCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm;
import com.marceloaleixo.treinoflow.service.CrmService;
import com.marceloaleixo.treinoflow.service.ScoreRiscoAlunoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/** Etapa 135: plano de ação financeiro por aluno, registrado no histórico CRM existente. */
@Controller
@RequestMapping("/financeiro/acoes-aluno")
public class PlanoAcaoFinanceiroController {
    private final UsuarioPersonalService personals;
    private final ScoreRiscoAlunoService riscos;
    private final CrmService crm;

    public PlanoAcaoFinanceiroController(UsuarioPersonalService personals, ScoreRiscoAlunoService riscos, CrmService crm) {
        this.personals = personals;
        this.riscos = riscos;
        this.crm = crm;
    }

    @GetMapping
    public String index(@RequestParam(required = false) Long alunoId, Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("alunos", riscos.listar(personal.getId()));
        model.addAttribute("canais", CanalCrm.values());
        model.addAttribute("resultados", ResultadoCrm.values());
        model.addAttribute("tipos", TipoInteracaoCrm.values());
        model.addAttribute("hoje", LocalDate.now());
        if (alunoId != null) {
            try {
                model.addAttribute("alunoSelecionado", crm.buscarAluno(personal.getId(), alunoId));
                model.addAttribute("historico", crm.timeline(personal.getId(), alunoId));
            } catch (IllegalArgumentException ex) {
                model.addAttribute("erro", ex.getMessage());
            }
        }
        return "financeiro/acoes-aluno";
    }

    @PostMapping
    public String salvar(@RequestParam Long alunoId,
                         @RequestParam String tipoAcao,
                         @RequestParam String assunto,
                         @RequestParam String descricao,
                         @RequestParam CanalCrm canal,
                         @RequestParam ResultadoCrm resultado,
                         @RequestParam(required = false) LocalDate proximaAcao,
                         Authentication authentication,
                         RedirectAttributes ra) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        try {
            if (proximaAcao != null && proximaAcao.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("A próxima ação não pode estar no passado.");
            }
            if (resultado == ResultadoCrm.EM_ACOMPANHAMENTO && proximaAcao == null) {
                throw new IllegalArgumentException("Defina a próxima ação para manter o acompanhamento.");
            }
            TipoInteracaoCrm tipo;
            try { tipo = TipoInteracaoCrm.valueOf(tipoAcao); }
            catch (Exception ex) { throw new IllegalArgumentException("Selecione um tipo de ação válido."); }
            InteracaoCrm interacao = new InteracaoCrm();
            interacao.setCanal(canal);
            interacao.setTipo(tipo);
            interacao.setResultado(resultado);
            interacao.setAssunto(assunto == null ? "" : assunto.trim());
            interacao.setDescricao(descricao == null ? "" : descricao.trim());
            interacao.setDataProximaAcao(proximaAcao);
            crm.salvar(personal.getId(), alunoId, interacao);
            ra.addFlashAttribute("sucesso", "Ação registrada no histórico do aluno.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/financeiro/acoes-aluno?alunoId=" + alunoId;
    }
}
