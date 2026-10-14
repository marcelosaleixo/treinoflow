package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.repository.NotificacaoRepository;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Disponibiliza a contagem de notificações não lidas no cabeçalho compartilhado. */
@ControllerAdvice(annotations = Controller.class)
public class NotificacaoMenuModelAdvice {
    private final UsuarioPersonalService usuarios;
    private final NotificacaoRepository notificacoes;

    public NotificacaoMenuModelAdvice(UsuarioPersonalService usuarios, NotificacaoRepository notificacoes) {
        this.usuarios = usuarios;
        this.notificacoes = notificacoes;
    }

    @ModelAttribute("notificacoesNaoLidasMenu")
    public long notificacoesNaoLidasMenu(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            return 0L;
        }
        try {
            UsuarioPersonal personal = usuarios.buscarPorEmail(authentication.getName());
            return notificacoes.countByPersonalIdAndLidaFalse(personal.getId());
        } catch (RuntimeException ex) {
            // A contagem do menu nunca deve impedir a renderização das demais páginas.
            return 0L;
        }
    }
}
