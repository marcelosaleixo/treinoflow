package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.dto.ScoreRiscoAlunoView;
import com.marceloaleixo.treinoflow.dto.RadarAcaoRegistro;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.CanalCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.service.RadarAcaoService;
import com.marceloaleixo.treinoflow.service.RadarReavaliacaoService;
import com.marceloaleixo.treinoflow.service.RecomendacaoAcaoPerfilService;
import com.marceloaleixo.treinoflow.service.RecomendacaoExplicacaoService;
import com.marceloaleixo.treinoflow.service.ComparadorEstrategiasService;
import com.marceloaleixo.treinoflow.service.ScoreRiscoAlunoService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/** Etapas 110/111: execução humana da ação e reavaliação automática pós-ação. */
@Controller
@RequestMapping("/performance/radar-diario")
public class RadarAcaoController {
    private final UsuarioPersonalService personals;
    private final RadarAcaoService acoes;
    private final ScoreRiscoAlunoService riscos;
    private final RadarReavaliacaoService reavaliacao;
    private final RecomendacaoAcaoPerfilService recomendacoes;
    private final RecomendacaoExplicacaoService explicacoes;
    private final ComparadorEstrategiasService comparador;

    public RadarAcaoController(UsuarioPersonalService personals,
                               RadarAcaoService acoes,
                               ScoreRiscoAlunoService riscos,
                               RadarReavaliacaoService reavaliacao,
                               RecomendacaoAcaoPerfilService recomendacoes,
                               RecomendacaoExplicacaoService explicacoes,
                               ComparadorEstrategiasService comparador) {
        this.personals = personals;
        this.acoes = acoes;
        this.riscos = riscos;
        this.reavaliacao = reavaliacao;
        this.recomendacoes = recomendacoes;
        this.explicacoes = explicacoes;
        this.comparador = comparador;
    }

    @GetMapping("/acao/{alunoId}")
    public String abrir(@PathVariable Long alunoId,
                        @RequestParam(required = false) String tipo,
                        @RequestParam(required = false) String titulo,
                        @RequestParam(required = false) String motivo,
                        @RequestParam(required = false) String acao,
                        Authentication authentication, Model model) {
        UsuarioPersonal personal = personal(authentication);
        model.addAttribute("personal", personal);
        var riscoAtual = riscos.buscar(personal.getId(), alunoId);
        model.addAttribute("acaoRadar", acoes.preparar(personal.getId(), alunoId, tipo, titulo, motivo, acao));
        model.addAttribute("riscoAtual", riscoAtual);
        var recomendacaoPerfil = recomendacoes.recomendar(personal.getId(), alunoId, riscoAtual.score());
        model.addAttribute("recomendacaoPerfil", recomendacaoPerfil);
        model.addAttribute("recomendacaoExplicacao", explicacoes.explicar(personal.getId(), alunoId, riscoAtual.score(), recomendacaoPerfil));
        model.addAttribute("comparacaoEstrategias", comparador.comparar(personal.getId(), alunoId, riscoAtual.score()));
        model.addAttribute("canais", CanalCrm.values());
        model.addAttribute("resultados", ResultadoCrm.values());
        model.addAttribute("hojeData", LocalDate.now());
        return "performance/radar-acao";
    }

    @PostMapping("/acao/{alunoId}")
    public String salvar(@PathVariable Long alunoId,
                         @RequestParam String tipo,
                         @RequestParam String assunto,
                         @RequestParam String motivo,
                         @RequestParam String acao,
                         @RequestParam CanalCrm canal,
                         @RequestParam ResultadoCrm resultado,
                         @RequestParam(required = false) LocalDate proximaAcao,
                         @RequestParam(required = false) String observacao,
                         Authentication authentication, RedirectAttributes ra) {
        try {
            UsuarioPersonal personal = personal(authentication);

            // Snapshot do risco antes da ação. É usado somente para comparação visual.
            ScoreRiscoAlunoView antes = riscos.buscar(personal.getId(), alunoId);

            RadarAcaoRegistro registro = new RadarAcaoRegistro(
                    personal.getId(), alunoId, tipo, assunto, motivo, acao,
                    canal, resultado, proximaAcao, observacao, antes.score());
            acoes.registrar(registro);

            // O contato recém-registrado já passa a compor o cálculo atual do radar.
            ScoreRiscoAlunoView depois = riscos.buscar(personal.getId(), alunoId);
            var leitura = reavaliacao.montar(antes, depois, resultado, proximaAcao, observacao);

            ra.addFlashAttribute("reavaliacao", leitura);
            return "redirect:/performance/radar-diario/acao/resultado/" + alunoId;
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/performance/radar-diario/acao/" + alunoId +
                    "?tipo=" + url(tipo) + "&titulo=" + url(assunto) + "&motivo=" + url(motivo) + "&acao=" + url(acao);
        }
    }

    @GetMapping("/acao/resultado/{alunoId}")
    public String resultado(@PathVariable Long alunoId,
                            Authentication authentication,
                            Model model) {
        UsuarioPersonal personal = personal(authentication);
        if (!model.containsAttribute("reavaliacao")) {
            return "redirect:/performance/radar-diario";
        }
        model.addAttribute("personal", personal);
        return "performance/radar-reavaliacao";
    }

    private UsuarioPersonal personal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuário não autenticado.");
        }
        return personals.buscarPorEmail(authentication.getName());
    }

    private String url(String valor) {
        return java.net.URLEncoder.encode(valor == null ? "" : valor, java.nio.charset.StandardCharsets.UTF_8);
    }
}
