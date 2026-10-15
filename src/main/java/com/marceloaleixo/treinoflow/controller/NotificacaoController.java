package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.Notificacao;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.TipoNotificacao;
import com.marceloaleixo.treinoflow.service.NotificacaoService;
import com.marceloaleixo.treinoflow.service.PreferenciaNotificacaoService;
import com.marceloaleixo.treinoflow.service.PreferenciaNotificacaoAuditoriaService;
import org.springframework.data.domain.Page;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Set;
import com.marceloaleixo.treinoflow.service.AlertaMetaReceitaPersistenteService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/notificacoes")
public class NotificacaoController {
    public record NotificacaoResumo(Long id, String titulo, String mensagem, String tipo,
                                    String dataCriacao, boolean lida) {}
    private final UsuarioPersonalService usuarios;
    private final NotificacaoService notificacoes;
    private final AlertaMetaReceitaPersistenteService alertasMetaReceita;
    private final com.marceloaleixo.treinoflow.service.NotificacaoSseService notificacoesSse;
    private final PreferenciaNotificacaoService preferencias;
    private final PreferenciaNotificacaoAuditoriaService auditoriaPreferencias;

    public NotificacaoController(UsuarioPersonalService usuarios, NotificacaoService notificacoes,
                                 AlertaMetaReceitaPersistenteService alertasMetaReceita,
                                 com.marceloaleixo.treinoflow.service.NotificacaoSseService notificacoesSse,
                                 PreferenciaNotificacaoService preferencias,
                                 PreferenciaNotificacaoAuditoriaService auditoriaPreferencias) {
        this.usuarios = usuarios;
        this.notificacoes = notificacoes;
        this.alertasMetaReceita = alertasMetaReceita;
        this.notificacoesSse = notificacoesSse;
        this.preferencias = preferencias;
        this.auditoriaPreferencias = auditoriaPreferencias;
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


    @GetMapping("/preferencias")
    public String preferencias(Authentication authentication, Model model) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        model.addAttribute("tiposNotificacao", TipoNotificacao.values());
        model.addAttribute("preferencias", preferencias.listar(personal));
        return "notificacoes/preferencias";
    }

    @GetMapping("/preferencias/historico")
    public String historicoPreferencias(@RequestParam(name = "pagina", defaultValue = "0") int pagina,
                                        Authentication authentication, Model model) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        Page<com.marceloaleixo.treinoflow.entity.PreferenciaNotificacaoAuditoria> historico =
                auditoriaPreferencias.listarDoPersonal(personal.getId(), pagina);
        model.addAttribute("historico", historico);
        model.addAttribute("paginaAtual", historico.getNumber());
        return "notificacoes/preferencias-historico";
    }

    @PostMapping("/preferencias")
    public String salvarPreferencias(@RequestParam(name = "tiposAtivos", required = false) List<TipoNotificacao> tiposAtivos,
                                     Authentication authentication, RedirectAttributes redirectAttributes) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        preferencias.salvar(personal, tiposAtivos == null ? Set.of() : Set.copyOf(tiposAtivos));
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Preferências de notificações salvas.");
        return "redirect:/notificacoes/preferencias";
    }

    /** Resumo JSON seguro para atualizar a central sem serializar entidades JPA. */
    @GetMapping(value = "/recentes", produces = "application/json")
    @ResponseBody
    public Map<String, Object> recentes(Authentication authentication) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        List<NotificacaoResumo> itens = notificacoes.ultimas(personal.getId()).stream()
                .map(n -> new NotificacaoResumo(n.getId(), n.getTitulo(), n.getMensagem(),
                        n.getTipo().name(), n.getDataCriacao() == null ? "" : n.getDataCriacao().toString(), n.isLida()))
                .collect(Collectors.toList());
        return Map.of("itens", itens, "totalNaoLidas", notificacoes.contarNaoLidas(personal.getId()));
    }

    /** Marca como lida por AJAX; a propriedade da notificação é sempre validada no serviço. */
    @PostMapping(value = "/{id}/ler-ajax", produces = "application/json")
    @ResponseBody
    public Map<String, Object> lerAjax(@PathVariable Long id, Authentication authentication) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        notificacoes.marcarLida(personal.getId(), id);
        return Map.of("sucesso", true, "totalNaoLidas", notificacoes.contarNaoLidas(personal.getId()));
    }

    @PostMapping(value = "/ler-todas-ajax", produces = "application/json")
    @ResponseBody
    public Map<String, Object> lerTodasAjax(Authentication authentication) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        notificacoes.marcarTodasLidas(personal.getId());
        return Map.of("sucesso", true, "totalNaoLidas", notificacoes.contarNaoLidas(personal.getId()));
    }

    /** Canal SSE autenticado, isolado por usuário personal. */
    @GetMapping(value = "/eventos", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter eventos(Authentication authentication) {
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        return notificacoesSse.conectar(personal.getId());
    }

    /** Endpoint leve para atualizar o contador do menu sem recarregar a página. */
    @GetMapping(value = "/contagem-nao-lidas", produces = "application/json")
    @ResponseBody
    public java.util.Map<String, Long> contagemNaoLidas(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            return java.util.Map.of("total", 0L);
        }
        UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
        return java.util.Map.of("total", notificacoes.contarNaoLidas(personal.getId()));
    }

}
