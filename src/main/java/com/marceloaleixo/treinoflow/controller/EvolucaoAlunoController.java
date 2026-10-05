package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.AlunoService;
import com.marceloaleixo.treinoflow.service.EvolucaoAlunoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/alunos")
public class EvolucaoAlunoController {
    private final AlunoService alunos;
    private final UsuarioPersonalService personals;
    private final EvolucaoAlunoService evolucao;

    public EvolucaoAlunoController(AlunoService alunos, UsuarioPersonalService personals, EvolucaoAlunoService evolucao) {
        this.alunos = alunos;
        this.personals = personals;
        this.evolucao = evolucao;
    }

    @GetMapping("/{id}/evolucao")
    public String evolucao(@PathVariable Long id, Authentication authentication, Model model) {
        UsuarioPersonal personal = personal(authentication);
        Aluno aluno = alunos.buscarPorId(id, personal.getId());
        model.addAttribute("aluno", aluno);
        model.addAttribute("evolucao", evolucao.montar(aluno));
        return "aluno/evolucao";
    }

    private UsuarioPersonal personal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuário não autenticado.");
        }
        return personals.buscarPorEmail(authentication.getName());
    }
}
