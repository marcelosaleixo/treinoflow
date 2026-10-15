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
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

/** Etapa 215: cobre acesso autorizado e decisões de negócio expostas pelos controladores reais. */
@WebMvcTest(controllers = {EntregaAlertaEmailAdminController.class, NotificacaoSaudeAdminController.class})
@Import(SecurityConfig.class)
class Etapa215AcessoAutorizadoRegrasNegocioTest {
    @Autowired private MockMvc mockMvc;

    @MockitoBean private FilaAlertaEmailService filaAlertaEmailService;
    @MockitoBean private ObservabilidadeNotificacaoService observabilidadeNotificacaoService;
    @MockitoBean private AlertaOperacionalNotificacaoService alertaOperacionalNotificacaoService;

    @Test
    @WithMockUser(roles = "MASTER")
    void masterPodeAbrirPainelDeEmails() throws Exception {
        when(filaAlertaEmailService.pesquisar(anyString(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of()));
        when(filaAlertaEmailService.obterIndicadores())
                .thenReturn(new FilaAlertaEmailService.Indicadores(0, 0, 0));

        mockMvc.perform(get("/admin/observabilidade/emails"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/observabilidade/emails"));
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void masterComCsrfPodeSolicitarReenvioERecebeSucessoQuandoServicoAceita() throws Exception {
        when(filaAlertaEmailService.reenviar("entrega-teste")).thenReturn(true);

        mockMvc.perform(post("/admin/observabilidade/emails/entrega-teste/reenviar").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/observabilidade/emails?status=FALHA_FINAL"))
                .andExpect(flash().attribute("sucesso", "Entrega colocada novamente na fila."));

        verify(filaAlertaEmailService).reenviar("entrega-teste");
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void reenvioRecusadoPeloServicoExibeMensagemSemDeclararSucesso() throws Exception {
        when(filaAlertaEmailService.reenviar("entrega-inexistente")).thenReturn(false);

        mockMvc.perform(post("/admin/observabilidade/emails/entrega-inexistente/reenviar").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("erro", "Não foi possível reenviar. Verifique se a entrega existe e está em falha final."));

        verify(filaAlertaEmailService).reenviar("entrega-inexistente");
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void masterPodeAbrirPainelDeNotificacoes() throws Exception {
        when(alertaOperacionalNotificacaoService.listarRecentes()).thenReturn(List.of());
        when(alertaOperacionalNotificacaoService.contarAbertos()).thenReturn(0L);

        mockMvc.perform(get("/admin/observabilidade/notificacoes"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/observabilidade/notificacoes"));
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void resolverIncidenteComCsrfUsaResultadoDoServico() throws Exception {
        when(alertaOperacionalNotificacaoService.resolver("incidente-teste")).thenReturn(true);

        mockMvc.perform(post("/admin/observabilidade/notificacoes/incidentes/incidente-teste/resolver").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("mensagemSucesso", "Incidente marcado como resolvido."));

        verify(alertaOperacionalNotificacaoService).resolver("incidente-teste");
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void incidenteNaoResolvidoPeloServicoExibeMensagemDeFalha() throws Exception {
        when(alertaOperacionalNotificacaoService.resolver("incidente-ja-resolvido")).thenReturn(false);

        mockMvc.perform(post("/admin/observabilidade/notificacoes/incidentes/incidente-ja-resolvido/resolver").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("mensagemErro", "O incidente não foi encontrado ou já foi resolvido."));

        verify(alertaOperacionalNotificacaoService).resolver("incidente-ja-resolvido");
    }
}
