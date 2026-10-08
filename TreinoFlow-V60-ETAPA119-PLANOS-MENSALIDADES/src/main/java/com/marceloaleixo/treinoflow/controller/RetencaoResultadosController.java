package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.service.RetencaoResultadosService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

@Controller
public class RetencaoResultadosController {
    private final RetencaoResultadosService service;
    private final UsuarioPersonalService usuarioPersonalService;

    public RetencaoResultadosController(RetencaoResultadosService service,
                                        UsuarioPersonalService usuarioPersonalService) {
        this.service = service;
        this.usuarioPersonalService = usuarioPersonalService;
    }

    @GetMapping("/retencao/resultados")
    public String resultados(@RequestParam(defaultValue = "30") int dias,
                             @RequestParam(required = false) String nivel,
                             Authentication authentication,
                             Model model) {
        UsuarioPersonal personal = personal(authentication);
        int periodo = dias == 90 ? 90 : 30;
        model.addAttribute("personal", personal);
        model.addAttribute("dias", periodo);
        model.addAttribute("nivel", nivel == null ? "" : nivel);
        model.addAttribute("dashboard", service.dashboard(personal.getId(), periodo));
        model.addAttribute("resultados", service.listar(personal.getId(), periodo, nivel));
        return "retencao/resultados";
    }

    @PostMapping("/retencao/resultados/{alunoId}/resultado")
    public String registrarResultado(@PathVariable Long alunoId,
                                     @RequestParam ResultadoCrm resultado,
                                     @RequestParam(defaultValue = "30") int dias,
                                     @RequestParam(required = false) String nivel,
                                     Authentication authentication,
                                     RedirectAttributes ra) {
        try {
            service.registrarResultado(personal(authentication).getId(), alunoId, resultado);
            ra.addFlashAttribute("sucesso", "Resultado registrado no CRM.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
        } catch (Exception ex) {
            ra.addFlashAttribute("erro", "Não foi possível registrar o resultado.");
        }
        StringBuilder redirect = new StringBuilder("redirect:/retencao/resultados?dias=").append(dias == 90 ? 90 : 30);
        if (nivel != null && !nivel.isBlank()) redirect.append("&nivel=").append(nivel);
        return redirect.toString();
    }

    private UsuarioPersonal personal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuário não autenticado.");
        }
        return usuarioPersonalService.buscarPorEmail(authentication.getName());
    }
}
