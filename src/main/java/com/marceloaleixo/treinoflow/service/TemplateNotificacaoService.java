package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.Notificacao;
import com.marceloaleixo.treinoflow.entity.TemplateNotificacao;
import com.marceloaleixo.treinoflow.enums.CanalNotificacao;
import com.marceloaleixo.treinoflow.enums.TipoNotificacao;
import com.marceloaleixo.treinoflow.repository.TemplateNotificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class TemplateNotificacaoService {
    private final TemplateNotificacaoRepository templates;

    public TemplateNotificacaoService(TemplateNotificacaoRepository templates) {
        this.templates = templates;
    }

    @Transactional(readOnly = true)
    public TemplateNotificacao buscarAtivo(TipoNotificacao tipo, CanalNotificacao canal) {
        return templates.findByTipoAndCanal(tipo, canal)
                .filter(TemplateNotificacao::isAtivo)
                .orElse(null);
    }

    public Conteudo renderizar(TipoNotificacao tipo, CanalNotificacao canal, Map<String, String> variaveis,
                               String tituloFallback, String mensagemFallback) {
        TemplateNotificacao template = buscarAtivo(tipo, canal);
        if (template == null) return new Conteudo(tituloFallback, mensagemFallback);
        return new Conteudo(substituir(template.getTitulo(), variaveis), substituir(template.getMensagem(), variaveis));
    }

    public void aplicarInterna(Notificacao notificacao, Map<String, String> variaveis) {
        Conteudo conteudo = renderizar(notificacao.getTipo(), CanalNotificacao.INTERNA, variaveis,
                notificacao.getTitulo(), notificacao.getMensagem());
        notificacao.setTitulo(limitar(conteudo.titulo(), 140));
        notificacao.setMensagem(limitar(conteudo.mensagem(), 500));
    }

    private String substituir(String texto, Map<String, String> variaveis) {
        if (texto == null) return "";
        String resultado = texto;
        for (Map.Entry<String, String> entry : variaveis.entrySet()) {
            resultado = resultado.replace("{{" + entry.getKey() + "}}", entry.getValue() == null ? "" : entry.getValue());
        }
        return resultado;
    }

    private String limitar(String valor, int max) {
        if (valor == null) return "";
        return valor.length() <= max ? valor : valor.substring(0, max);
    }

    public record Conteudo(String titulo, String mensagem) {}
}
