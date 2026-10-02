package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.AvaliacaoFisica;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.AlunoService;
import com.marceloaleixo.treinoflow.service.AvaliacaoFisicaService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/alunos/{alunoId}/avaliacoes")
public class AvaliacaoFisicaController {
    @Autowired private AvaliacaoFisicaService avaliacaoService;
    @Autowired private AlunoService alunoService;
    @Autowired private UsuarioPersonalService usuarioPersonalService;

    @GetMapping
    public String listar(@PathVariable Long alunoId, Authentication auth, Model model,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "5") int size) {
        Long personalId = personal(auth).getId();
        model.addAttribute("aluno", alunoService.buscarPorId(alunoId, personalId));
        int paginaAtual = Math.max(0, page);
        int tamanhoPagina = Math.min(50, Math.max(5, size));
        model.addAttribute("pagina", avaliacaoService.listarPaginado(alunoId, personalId, PageRequest.of(paginaAtual, tamanhoPagina)));
        return "avaliacao/lista";
    }

    @GetMapping("/relatorio")
    public String relatorio(@PathVariable Long alunoId, Authentication auth, Model model) {
        Long personalId = personal(auth).getId();
        Aluno aluno = alunoService.buscarPorId(alunoId, personalId);
        model.addAttribute("aluno", aluno);
        model.addAttribute("avaliacoes", avaliacaoService.listar(alunoId, personalId));
        model.addAttribute("dataEmissao", java.time.LocalDate.now());
        return "avaliacao/relatorio";
    }

    @GetMapping("/evolucao")
    public String evolucao(@PathVariable Long alunoId, Authentication auth, Model model) {
        Long personalId = personal(auth).getId();
        Aluno aluno = alunoService.buscarPorId(alunoId, personalId);
        java.util.List<AvaliacaoFisica> historico = new java.util.ArrayList<>(avaliacaoService.listar(alunoId, personalId));
        java.util.Collections.reverse(historico); // ordem cronológica para os gráficos
        model.addAttribute("aluno", aluno);
        model.addAttribute("avaliacoes", historico);
        model.addAttribute("datas", historico.stream().map(a -> a.getDataAvaliacao().toString()).toList());
        model.addAttribute("pesos", historico.stream().map(a -> a.getPesoKg() == null ? null : a.getPesoKg().doubleValue()).toList());
        model.addAttribute("cinturas", historico.stream().map(a -> a.getCinturaCm() == null ? null : a.getCinturaCm().doubleValue()).toList());
        model.addAttribute("quadris", historico.stream().map(a -> a.getQuadrilCm() == null ? null : a.getQuadrilCm().doubleValue()).toList());
        model.addAttribute("gorduras", historico.stream().map(a -> a.getPercentualGordura() == null ? null : a.getPercentualGordura().doubleValue()).toList());
        model.addAttribute("totalAvaliacoes", historico.size());
        return "avaliacao/evolucao";
    }

    @GetMapping("/nova")
    public String nova(@PathVariable Long alunoId, Authentication auth, Model model) {
        model.addAttribute("aluno", alunoService.buscarPorId(alunoId, personal(auth).getId()));
        model.addAttribute("avaliacao", new AvaliacaoFisica());
        model.addAttribute("titulo", "Nova avaliação física");
        return "avaliacao/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long alunoId, @PathVariable Long id, Authentication auth, Model model) {
        Long personalId = personal(auth).getId();
        model.addAttribute("aluno", alunoService.buscarPorId(alunoId, personalId));
        model.addAttribute("avaliacao", avaliacaoService.buscar(id, alunoId, personalId));
        model.addAttribute("titulo", "Editar avaliação física");
        return "avaliacao/form";
    }

    @PostMapping("/salvar")
    public String salvar(@PathVariable Long alunoId, @ModelAttribute AvaliacaoFisica avaliacao,
                         Authentication auth, RedirectAttributes flash) {
        try {
            avaliacaoService.salvar(alunoId, personal(auth).getId(), avaliacao);
            flash.addFlashAttribute("sucesso", "Avaliação salva com sucesso.");
            return "redirect:/alunos/" + alunoId + "/avaliacoes";
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/alunos/" + alunoId + (avaliacao.getId() == null ? "/avaliacoes/nova" : "/avaliacoes/" + avaliacao.getId() + "/editar");
        }
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long alunoId, @PathVariable Long id, Authentication auth, RedirectAttributes flash) {
        try {
            avaliacaoService.excluir(id, alunoId, personal(auth).getId());
            flash.addFlashAttribute("sucesso", "Avaliação excluída.");
        } catch (IllegalArgumentException ex) { flash.addFlashAttribute("erro", ex.getMessage()); }
        return "redirect:/alunos/" + alunoId + "/avaliacoes";
    }

    private UsuarioPersonal personal(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) throw new IllegalArgumentException("Usuário não autenticado.");
        return usuarioPersonalService.buscarPorEmail(auth.getName());
    }
}
