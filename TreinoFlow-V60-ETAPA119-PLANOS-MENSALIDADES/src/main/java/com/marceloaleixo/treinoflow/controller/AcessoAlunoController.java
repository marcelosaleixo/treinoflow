package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.dto.TreinoAlunoView;
import com.marceloaleixo.treinoflow.service.AcessoAlunoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AcessoAlunoController {
    @Autowired
    private AcessoAlunoService acessoAlunoService;

    @GetMapping("/a/{token}")
    public String visualizarTreino(@PathVariable String token, Model model) {
        try {
            TreinoAlunoView treino = acessoAlunoService.buscarTreinoLiberado(token);
            model.addAttribute("treino", treino);
            return "aluno/treino-publico";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("mensagemErro", ex.getMessage());
            return "aluno/treino-indisponivel";
        }
    }
}
