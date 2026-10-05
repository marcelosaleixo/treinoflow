package com.marceloaleixo.treinoflow.web;

import com.marceloaleixo.treinoflow.entity.Assinatura;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.service.AssinaturaService;
import com.marceloaleixo.treinoflow.service.UsuarioPersonalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AssinaturaAcessoInterceptor implements HandlerInterceptor {

    private final UsuarioPersonalService usuarioPersonalService;
    private final AssinaturaService assinaturaService;

    public AssinaturaAcessoInterceptor(UsuarioPersonalService usuarioPersonalService,
                                       AssinaturaService assinaturaService) {
        this.usuarioPersonalService = usuarioPersonalService;
        this.assinaturaService = assinaturaService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication authentication = (Authentication) request.getUserPrincipal();

        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }

        if (temRole(authentication, "ROLE_MASTER")) {
            return true;
        }

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String caminho = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;

        // A página de assinatura precisa permanecer acessível para que o Personal
        // consiga entender por que o acesso foi limitado e consultar sua situação.
        if (caminho.startsWith("/assinatura")
                || caminho.startsWith("/logout")
                || caminho.startsWith("/error")
                || caminho.startsWith("/css/")
                || caminho.startsWith("/js/")
                || caminho.startsWith("/images/")
                || caminho.startsWith("/a/")) {
            return true;
        }

        UsuarioPersonal personal = usuarioPersonalService.buscarPorEmail(authentication.getName());
        if (personal == null || personal.getId() == null) {
            response.sendRedirect(contextPath + "/login?assinatura=indisponivel");
            return false;
        }

        assinaturaService.atualizarVencida(personal.getId());
        Optional<Assinatura> assinatura = assinaturaService.buscarPorPersonal(personal.getId());

        if (assinatura.isPresent() && assinatura.get().permiteAcesso() && !assinatura.get().estaVencida()) {
            return true;
        }

        response.sendRedirect(contextPath + "/assinatura/bloqueada");
        return false;
    }

    private boolean temRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}
