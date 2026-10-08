package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.ExperimentoRetencaoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ExperimentoRetencaoController {
 private final ExperimentoRetencaoService service; private final UsuarioPersonalService usuarios;
 public ExperimentoRetencaoController(ExperimentoRetencaoService service, UsuarioPersonalService usuarios){this.service=service;this.usuarios=usuarios;}
 @GetMapping("/assistente/experimentos") public String index(Authentication auth, Model model){UsuarioPersonal p=personal(auth); model.addAttribute("personal",p); model.addAttribute("experimentos",service.listar(p.getId())); return "assistente/experimentos";}
 @PostMapping("/assistente/experimentos/{id}/encerrar") public String encerrar(@PathVariable Long id, Authentication auth, RedirectAttributes ra){try{service.encerrar(personal(auth).getId(),id);ra.addFlashAttribute("sucesso","Experimento encerrado.");}catch(IllegalArgumentException e){ra.addFlashAttribute("erro",e.getMessage());}return "redirect:/assistente/experimentos";}
 private UsuarioPersonal personal(Authentication a){if(a==null||!a.isAuthenticated())throw new IllegalArgumentException("Usuário não autenticado.");return usuarios.buscarPorEmail(a.getName());}
}
