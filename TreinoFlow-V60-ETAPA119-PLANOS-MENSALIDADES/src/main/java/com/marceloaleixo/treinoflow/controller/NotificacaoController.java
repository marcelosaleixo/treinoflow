package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.Notificacao;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.TipoNotificacao;
import com.marceloaleixo.treinoflow.service.NotificacaoService;
import com.marceloaleixo.treinoflow.service.AlertaMetaReceitaPersistenteService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/notificacoes")
public class NotificacaoController {
    private final UsuarioPersonalService usuarios;
    private final NotificacaoService notificacoes;
    private final AlertaMetaReceitaPersistenteService alertasMetaReceita;

    public NotificacaoController(UsuarioPersonalService usuarios, NotificacaoService notificacoes,
                                 AlertaMetaReceitaPersistenteService alertasMetaReceita) {
        this.usuarios = usuarios;
        this.notificacoes = notificacoes;
        this.alertasMetaReceita = alertasMetaReceita;
    }

    @GetMapping
    public String lista(@RequestParam(name = "estado", required = false) String estado,
                        @RequestParam(name = "mes", required = false) String mes,
                        @RequestParam(name = "tipo", required = false) String tipo,
                        Authentication authentication, Model model) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        // Sincroniza os alertas do mês atual antes de montar a central, para evitar conteúdo financeiro desatualizado.
        alertasMetaReceita.sincronizar(personal, YearMonth.now());
        List<Notificacao> todas = notificacoes.listar(personal.getId());
        List<Notificacao> filtradas = new ArrayList<>();
        YearMonth periodo = null;
        if (mes != null && !mes.isBlank()) {
            try {
                periodo = YearMonth.parse(mes);
            } catch (RuntimeException ignored) {
                mes = "";
            }
        }
        for (Notificacao n : todas) {
            if ("nao-lidas".equals(estado) && n.isLida()) continue;
            if ("lidas".equals(estado) && !n.isLida()) continue;
            if (tipo != null && !tipo.isBlank() && !n.getTipo().name().equals(tipo)) continue;
            if (periodo != null && (n.getDataCriacao() == null
                    || !YearMonth.from(n.getDataCriacao()).equals(periodo))) continue;
            filtradas.add(n);
        }
        model.addAttribute("notificacoes", filtradas);
        model.addAttribute("tiposNotificacao", TipoNotificacao.values());
        model.addAttribute("estadoSelecionado", estado == null ? "todas" : estado);
        model.addAttribute("mesSelecionado", mes == null ? "" : mes);
        model.addAttribute("tipoSelecionado", tipo == null ? "" : tipo);
        model.addAttribute("totalNotificacoes", todas.size());
        model.addAttribute("totalNaoLidas", notificacoes.contarNaoLidas(personal.getId()));
        return "notificacoes/lista";
    }

    @PostMapping("/{id}/ler")
    public String ler(@PathVariable Long id, Authentication authentication) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        notificacoes.marcarLida(personal.getId(), id);
        return "redirect:/notificacoes";
    }

    @PostMapping("/ler-todas")
    public String lerTodas(Authentication authentication) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        notificacoes.marcarTodasLidas(personal.getId());
        return "redirect:/notificacoes";
    }
}
