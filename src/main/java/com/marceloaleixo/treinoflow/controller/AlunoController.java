package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.ObjetivoAluno;
import com.marceloaleixo.treinoflow.service.AlunoService;
import com.marceloaleixo.treinoflow.service.PortalAlunoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AlunoController {

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private UsuarioPersonalService usuarioPersonalService;

    @Autowired
    private PortalAlunoService portalAlunoService;

    @GetMapping("/alunos")
    public String listar(Authentication authentication, Model model,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "5") int size,
                         @RequestParam(required = false) String q) {
        UsuarioPersonal personal = personalAutenticado(authentication);
        int paginaAtual = Math.max(0, page);
        int tamanhoPagina = Math.min(50, Math.max(5, size));
        model.addAttribute("pagina", alunoService.listarPaginado(personal.getId(), q, PageRequest.of(paginaAtual, tamanhoPagina)));
        model.addAttribute("q", q == null ? "" : q.trim());
        model.addAttribute("personal", personal);
        return "aluno/lista";
    }

    @GetMapping("/alunos/novo")
    public String novo(Model model) {
        model.addAttribute("aluno", new Aluno());
        model.addAttribute("titulo", "Novo aluno");
        adicionarObjetivos(model);
        return "aluno/form";
    }

    @GetMapping("/alunos/{id}/editar")
    public String editar(@PathVariable Long id, Authentication authentication, Model model) {
        UsuarioPersonal personal = personalAutenticado(authentication);
        model.addAttribute("aluno", alunoService.buscarPorId(id, personal.getId()));
        model.addAttribute("titulo", "Editar aluno");
        adicionarObjetivos(model);
        return "aluno/form";
    }

    @GetMapping("/alunos/{id}/portal")
    public String abrirPortal(@PathVariable Long id, Authentication authentication) {
        UsuarioPersonal personal = personalAutenticado(authentication);
        String token = portalAlunoService.obterOuCriarToken(id, personal.getId());
        return "redirect:/portal/" + token;
    }

    @PostMapping("/alunos/salvar")
    public String salvar(@ModelAttribute("aluno") Aluno aluno,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        try {
            UsuarioPersonal personal = personalAutenticado(authentication);
            alunoService.salvar(personal.getId(), aluno);
            redirectAttributes.addFlashAttribute("sucesso", "Aluno salvo com sucesso.");
            return "redirect:/alunos";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
            return aluno.getId() == null ? "redirect:/alunos/novo" : "redirect:/alunos/" + aluno.getId() + "/editar";
        }
    }

    @PostMapping("/alunos/{id}/excluir")
    public String excluir(@PathVariable Long id,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        try {
            UsuarioPersonal personal = personalAutenticado(authentication);
            alunoService.excluir(id, personal.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Aluno excluído com sucesso.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/alunos";
    }

    private void adicionarObjetivos(Model model) {
        model.addAttribute("objetivos", java.util.Arrays.stream(ObjetivoAluno.values())
                .map(ObjetivoAluno::getDescricao)
                .toList());
    }

    private UsuarioPersonal personalAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuário não autenticado.");
        }
        return usuarioPersonalService.buscarPorEmail(authentication.getName());
    }
}
