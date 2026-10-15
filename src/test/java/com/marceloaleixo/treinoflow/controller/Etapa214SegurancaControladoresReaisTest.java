package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.config.SecurityConfig;
import com.marceloaleixo.treinoflow.service.AlertaOperacionalNotificacaoService;
import com.marceloaleixo.treinoflow.service.FilaAlertaEmailService;
import com.marceloaleixo.treinoflow.service.ObservabilidadeNotificacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Etapa 214: verifica autorização e CSRF nos controladores administrativos reais. */
@WebMvcTest(controllers = {EntregaAlertaEmailAdminController.class, NotificacaoSaudeAdminController.class})
@Import(SecurityConfig.class)
class Etapa214SegurancaControladoresReaisTest {
    @Autowired private MockMvc mockMvc;

    @MockitoBean private FilaAlertaEmailService filaAlertaEmailService;
    @MockitoBean private ObservabilidadeNotificacaoService observabilidadeNotificacaoService;
    @MockitoBean private AlertaOperacionalNotificacaoService alertaOperacionalNotificacaoService;

    @Test
    void visitanteEhRedirecionadoAoPainelRealDeEmails() throws Exception {
        mockMvc.perform(get("/admin/observabilidade/emails"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "PERSONAL")
    void personalNaoAcessaPainelRealDeNotificacoes() throws Exception {
        mockMvc.perform(get("/admin/observabilidade/notificacoes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PERSONAL")
    void personalNaoPodeAcionarReenvioAdministrativo() throws Exception {
        mockMvc.perform(post("/admin/observabilidade/emails/entrega-teste/reenviar").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void masterSemCsrfNaoPodeResolverIncidente() throws Exception {
        mockMvc.perform(post("/admin/observabilidade/notificacoes/incidentes/incidente-teste/resolver"))
                .andExpect(status().isForbidden());
    }
}
