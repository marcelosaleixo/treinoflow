package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.EvolucaoInteligenteAlunoService;
import com.marceloaleixo.treinoflow.service.TreinoExercicioService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EvolucaoInteligenteAlunoController {
    private final UsuarioPersonalService personals;
    private final EvolucaoInteligenteAlunoService service;
    private final TreinoExercicioService treinoExercicioService;
    private final com.marceloaleixo.treinoflow.service.FeedbackPrescricaoService feedbackPrescricaoService;

    public EvolucaoInteligenteAlunoController(UsuarioPersonalService personals,
                                               EvolucaoInteligenteAlunoService service,
                                               TreinoExercicioService treinoExercicioService,
                                               com.marceloaleixo.treinoflow.service.FeedbackPrescricaoService feedbackPrescricaoService) {
        this.personals = personals;
        this.service = service;
        this.treinoExercicioService = treinoExercicioService;
        this.feedbackPrescricaoService = feedbackPrescricaoService;
    }

    @PostMapping("/performance/alunos/{alunoId}/evolucao/exercicios/{itemId}/aplicar-prescricao")
    public String aplicarPrescricao(@PathVariable Long alunoId, @PathVariable Long itemId,
                                    Authentication authentication, RedirectAttributes flash) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        try {
            treinoExercicioService.aplicarPrescricaoAssistida(itemId, alunoId, personal.getId());
            flash.addFlashAttribute("sucesso", "Prescrição assistida aplicada. Revise o treino antes de liberá-lo ao aluno.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/performance/alunos/" + alunoId + "/evolucao";
    }

    @PostMapping("/performance/alunos/{alunoId}/evolucao/auditorias/{auditoriaId}/feedback")
    public String feedbackPrescricao(@PathVariable Long alunoId, @PathVariable Long auditoriaId,
                                     @org.springframework.web.bind.annotation.RequestParam String feedback,
                                     @org.springframework.web.bind.annotation.RequestParam(required = false) String motivo,
                                     Authentication authentication, RedirectAttributes flash) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        try {
            feedbackPrescricaoService.registrar(auditoriaId, alunoId, personal.getId(), feedback, motivo);
            flash.addFlashAttribute("sucesso", "Feedback registrado. O TreinoFlow usará essa decisão para aprimorar a leitura das recomendações.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/performance/alunos/" + alunoId + "/evolucao";
    }

    @GetMapping("/performance/alunos/{alunoId}/evolucao")
    public String evolucao(@PathVariable Long alunoId, Authentication authentication, Model model) {
        UsuarioPersonal personal = personals.buscarPorEmail(authentication.getName());
        model.addAttribute("personal", personal);
        model.addAttribute("evolucao", service.montar(alunoId, personal.getId()));
        model.addAttribute("historicoPrescricoes", service.historicoPrescricoes(alunoId, personal.getId()));
        return "performance/aluno-evolucao";
    }
}
