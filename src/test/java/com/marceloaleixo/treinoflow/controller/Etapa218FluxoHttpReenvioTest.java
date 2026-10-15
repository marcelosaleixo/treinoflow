package com.marceloaleixo.treinoflow.controller;

import com.marceloaleixo.treinoflow.config.SecurityConfig;
import com.marceloaleixo.treinoflow.service.FilaAlertaEmailService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Etapa 218: cobre o fluxo HTTP real do controlador de reenvio e suas barreiras de segurança. */
@WebMvcTest(controllers = EntregaAlertaEmailAdminController.class)
@Import(SecurityConfig.class)
class Etapa218FluxoHttpReenvioTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private FilaAlertaEmailService fila;

    @Test
    void visitanteERedirecionadoParaLoginAoAbrirPainel() throws Exception {
        mockMvc.perform(get("/admin/observabilidade/emails"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        verifyNoInteractions(fila);
    }

    @Test
    @WithMockUser(roles = "PERSONAL")
    void personalNaoPodeReenviarAlertaAdministrativo() throws Exception {
        mockMvc.perform(post("/admin/observabilidade/emails/entrega-218/reenviar").with(csrf()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(fila);
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void postSemCsrfERejeitadoAntesDeChamarServico() throws Exception {
        mockMvc.perform(post("/admin/observabilidade/emails/entrega-218/reenviar"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(fila);
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void masterComCsrfRecebeRedirectEFeedbackAposReenvioAceito() throws Exception {
        when(fila.reenviar("entrega-218")).thenReturn(true);

        mockMvc.perform(post("/admin/observabilidade/emails/entrega-218/reenviar").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/admin/observabilidade/emails?status=FALHA_FINAL"))
                .andExpect(flash().attribute("sucesso", "Entrega colocada novamente na fila."));

        verify(fila, times(1)).reenviar("entrega-218");
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void reenvioRepetidoRecusadoNaoExibeMensagemDeSucesso() throws Exception {
        when(fila.reenviar("entrega-218")).thenReturn(true, false);

        mockMvc.perform(post("/admin/observabilidade/emails/entrega-218/reenviar").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("sucesso"));
        mockMvc.perform(post("/admin/observabilidade/emails/entrega-218/reenviar").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("erro", "Não foi possível reenviar. Verifique se a entrega existe e está em falha final."))
                .andExpect(flash().attributeCount(1));

        verify(fila, times(2)).reenviar("entrega-218");
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void painelNormalizaFiltroDeStatusEPaginaNegativa() throws Exception {
        when(fila.pesquisar(eq("TODOS"), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(new PageImpl<>(List.of()));
        when(fila.obterIndicadores()).thenReturn(new FilaAlertaEmailService.Indicadores(0, 0, 0));

        mockMvc.perform(get("/admin/observabilidade/emails")
                        .param("status", "STATUS_INVALIDO")
                        .param("pagina", "-4"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/observabilidade/emails"))
                .andExpect(model().attribute("statusSelecionado", "TODOS"))
                .andExpect(model().attribute("paginaAtual", 0));
    }
}
