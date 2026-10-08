package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.AutomacaoRetencaoInteligenteService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AutomacaoRetencaoController {
    private final AutomacaoRetencaoInteligenteService service;
    private final UsuarioPersonalService usuarios;

    public AutomacaoRetencaoController(AutomacaoRetencaoInteligenteService service, UsuarioPersonalService usuarios) {
        this.service = service; this.usuarios = usuarios;
    }

    @GetMapping("/assistente/automacao")
    public String index(Authentication authentication, Model model) {
        UsuarioPersonal personal = personal(authentication);
        model.addAttribute("personal", personal);
        model.addAttribute("config", service.obterOuCriar(personal.getId()));
        return "assistente/automacao";
    }

    @PostMapping("/assistente/automacao")
    public String salvar(Authentication authentication,
                          @RequestParam(defaultValue="false") boolean ativa,
                          @RequestParam(defaultValue="false") boolean whatsappCritico,
                          @RequestParam(defaultValue="false") boolean followUpAlto,
                          @RequestParam int scoreMinimo,
                          @RequestParam int maxAcoesDia,
                          @RequestParam int cooldownDias,
                          @RequestParam String horaInicio,
                          @RequestParam String horaFim,
                          @RequestParam BigDecimal valorMensalAlunoEstimado,
                          @RequestParam BigDecimal custoAutomacaoMensal,
                          RedirectAttributes ra) {
        try {
            UsuarioPersonal p=personal(authentication);
            service.salvar(p.getId(),ativa,whatsappCritico,followUpAlto,scoreMinimo,maxAcoesDia,cooldownDias,horaInicio,horaFim,valorMensalAlunoEstimado,custoAutomacaoMensal);
            ra.addFlashAttribute("sucesso","Configuração de automação salva com sucesso.");
        } catch (IllegalArgumentException ex) { ra.addFlashAttribute("erro",ex.getMessage()); }
        return "redirect:/assistente/automacao";
    }

    @PostMapping("/assistente/automacao/executar")
    public String executar(Authentication authentication, RedirectAttributes ra) {
        try {
            UsuarioPersonal p=personal(authentication);
            var r=service.executar(p.getId());
            ra.addFlashAttribute("sucesso",r.mensagem()+" Avaliadas: "+r.avaliadas()+" · Executadas: "+r.executadas()+" · Ignoradas: "+r.ignoradas()+".");
        } catch (RuntimeException ex) { ra.addFlashAttribute("erro",ex.getMessage()); }
        return "redirect:/assistente/automacao";
    }

    private UsuarioPersonal personal(Authentication authentication) {
        if(authentication==null||!authentication.isAuthenticated()) throw new IllegalArgumentException("Usuário não autenticado.");
        return usuarios.buscarPorEmail(authentication.getName());
    }
}
