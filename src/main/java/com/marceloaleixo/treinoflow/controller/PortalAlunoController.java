package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.Treino;
import com.marceloaleixo.treinoflow.service.EvolucaoAlunoService;
import com.marceloaleixo.treinoflow.service.PortalAlunoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/portal")
public class PortalAlunoController {
    private final PortalAlunoService portal;
    private final EvolucaoAlunoService evolucao;
    private final com.marceloaleixo.treinoflow.service.ProgressaoInteligenteService progressao;

    public PortalAlunoController(PortalAlunoService portal, EvolucaoAlunoService evolucao, com.marceloaleixo.treinoflow.service.ProgressaoInteligenteService progressao) {
        this.portal = portal;
        this.evolucao = evolucao;
        this.progressao = progressao;
    }

    @GetMapping("/{token}")
    public String inicio(@PathVariable String token, Model model) {
        try {
            Aluno aluno = portal.buscarAlunoPorToken(token);
            model.addAttribute("aluno", aluno);
            model.addAttribute("treinos", portal.listarTreinos(aluno));
            model.addAttribute("historico", portal.historico(aluno));
            model.addAttribute("agendamentos", portal.listarAgendamentosProximos(aluno));
            model.addAttribute("evolucao", evolucao.montar(aluno));
            model.addAttribute("token", token);
            return "portal/index";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("erro", ex.getMessage());
            return "portal/indisponivel";
        }
    }

    @PostMapping("/{token}/agendamentos/{agendamentoId}/confirmar")
    public String confirmarAgendamento(@PathVariable String token, @PathVariable Long agendamentoId,
                                       RedirectAttributes ra) {
        try {
            Aluno aluno = portal.buscarAlunoPorToken(token);
            portal.confirmarAgendamento(aluno, agendamentoId);
            ra.addFlashAttribute("sucesso", "Treino confirmado com sucesso. Nos vemos lá! 💪");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/portal/" + token;
    }

    @PostMapping("/{token}/agendamentos/{agendamentoId}/cancelar")
    public String cancelarAgendamento(@PathVariable String token, @PathVariable Long agendamentoId,
                                      RedirectAttributes ra) {
        try {
            Aluno aluno = portal.buscarAlunoPorToken(token);
            portal.cancelarAgendamento(aluno, agendamentoId);
            ra.addFlashAttribute("sucesso", "Agendamento cancelado. Se precisar, combine um novo horário com seu Personal.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/portal/" + token;
    }

    @GetMapping("/{token}/treinos/{treinoId}")
    public String treino(@PathVariable String token, @PathVariable Long treinoId, Model model) {
        try {
            Aluno aluno = portal.buscarAlunoPorToken(token);
            Treino treino = portal.buscarTreino(aluno, treinoId);
            model.addAttribute("aluno", aluno);
            model.addAttribute("treino", treino);
            model.addAttribute("exercicios", portal.listarExercicios(treino));
            model.addAttribute("ultimaExecucao", portal.ultimaExecucao(treino));
            model.addAttribute("exerciciosConcluidos", portal.exerciciosConcluidosHoje(treino));
            model.addAttribute("seriesHoje", portal.seriesHoje(treino));
            model.addAttribute("ultimaSerieAnterior", portal.ultimaSerieAnteriorPorExercicio(treino, aluno));
            Map<Long, com.marceloaleixo.treinoflow.dto.RecomendacaoProgressaoView> recomendacoesProgressao = new HashMap<>();
            for (var item : portal.listarExercicios(treino)) {
                recomendacoesProgressao.put(item.getId(), progressao.recomendar(item, aluno));
            }
            model.addAttribute("recomendacoesProgressao", recomendacoesProgressao);
            model.addAttribute("token", token);
            return "portal/treino";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("erro", ex.getMessage());
            return "portal/indisponivel";
        }
    }

    @PostMapping("/{token}/treinos/{treinoId}/exercicios/{treinoExercicioId}/series")
    @ResponseBody
    public Map<String, Object> registrarSerie(@PathVariable String token,
                                               @PathVariable Long treinoId,
                                               @PathVariable Long treinoExercicioId,
                                               @RequestParam Integer numeroSerie,
                                               @RequestParam(required = false) String carga,
                                               @RequestParam(required = false) Integer repeticoes,
                                               @RequestParam(required = false) Integer rpe,
                                               @RequestParam(required = false) String observacao) {
        Map<String, Object> resposta = new HashMap<>();
        try {
            Aluno aluno = portal.buscarAlunoPorToken(token);
            var serie = portal.registrarSerie(aluno, treinoId, treinoExercicioId, numeroSerie, carga, repeticoes, rpe, observacao);
            resposta.put("sucesso", true);
            resposta.put("serie", serie.getNumeroSerie());
            resposta.put("rpe", serie.getRpe());
            resposta.put("mensagem", "Série " + serie.getNumeroSerie() + " registrada.");
            return resposta;
        } catch (IllegalArgumentException ex) {
            resposta.put("sucesso", false);
            resposta.put("mensagem", ex.getMessage());
            return resposta;
        }
    }

    @PostMapping("/{token}/treinos/{treinoId}/concluir")
    public String concluir(@PathVariable String token, @PathVariable Long treinoId,
                           @RequestParam(required = false) Integer nota,
                           @RequestParam(required = false) String feedback,
                           @RequestParam Map<String, String> parametros,
                           RedirectAttributes ra) {
        try {
            Aluno aluno = portal.buscarAlunoPorToken(token);
            portal.registrarConclusao(aluno, treinoId, nota, feedback, parametros);
            ra.addFlashAttribute("sucesso", "Progresso do treino salvo com sucesso.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/portal/" + token + "/treinos/" + treinoId;
    }

}
