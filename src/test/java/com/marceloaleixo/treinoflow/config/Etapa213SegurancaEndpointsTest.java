package com.marceloaleixo.treinoflow.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Etapa 213: testes focados nas regras de autorização definidas em SecurityConfig. */
@WebMvcTest(controllers = Etapa213SegurancaEndpointsTest.SecurityProbeController.class)
@Import(SecurityConfig.class)
class Etapa213SegurancaEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void visitanteNaoAcessaRotaProtegida() throws Exception {
        mockMvc.perform(get("/probe/privado"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "PERSONAL")
    void personalNaoAcessaAreaMaster() throws Exception {
        mockMvc.perform(get("/admin/probe"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void masterAcessaAreaAdministrativa() throws Exception {
        mockMvc.perform(get("/admin/probe"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PERSONAL")
    void postSemCsrfEhRejeitado() throws Exception {
        mockMvc.perform(post("/probe/acao"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PERSONAL")
    void postComCsrfValidoEhPermitido() throws Exception {
        mockMvc.perform(post("/probe/acao").with(csrf()))
                .andExpect(status().isOk());
    }

    @RestController
    static class SecurityProbeController {
        @GetMapping("/admin/probe")
        String admin() { return "ok"; }

        @GetMapping("/probe/privado")
        String privado() { return "ok"; }

        @PostMapping("/probe/acao")
        String acao() { return "ok"; }
    }
}
