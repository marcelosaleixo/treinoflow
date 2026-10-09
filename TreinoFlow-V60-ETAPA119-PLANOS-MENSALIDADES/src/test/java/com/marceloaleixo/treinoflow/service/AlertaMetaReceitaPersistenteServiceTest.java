package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.MetaReceitaAcoes;
import com.marceloaleixo.treinoflow.entity.Notificacao;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.TipoNotificacao;
import com.marceloaleixo.treinoflow.repository.InteracaoRecebimentoVinculoRepository;
import com.marceloaleixo.treinoflow.repository.MetaReceitaAcoesRepository;
import com.marceloaleixo.treinoflow.repository.NotificacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AlertaMetaReceitaPersistenteServiceTest {
    private NotificacaoRepository notificacoes;
    private MetaReceitaAcoesRepository metas;
    private InteracaoRecebimentoVinculoRepository vinculos;
    private AlertaMetaReceitaPersistenteService service;

    @BeforeEach
    void configurar() {
        notificacoes = mock(NotificacaoRepository.class);
        metas = mock(MetaReceitaAcoesRepository.class);
        vinculos = mock(InteracaoRecebimentoVinculoRepository.class);
        service = new AlertaMetaReceitaPersistenteService(notificacoes, metas, vinculos);
        when(vinculos.findByInteracaoPersonalIdOrderByDataVinculoDesc(any())).thenReturn(List.of());
        when(notificacoes.findByChaveUnica(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void ignoraPersonalSemIdentificadorSemConsultarRepositorios() {
        UsuarioPersonal personal = new UsuarioPersonal();

        service.sincronizar(personal, YearMonth.now());

        verifyNoInteractions(notificacoes, metas, vinculos);
    }

    @Test
    void criaAlertaQuandoMetaDoMesAtualNaoFoiDefinida() {
        UsuarioPersonal personal = personal(17L);
        YearMonth periodo = YearMonth.now();
        when(metas.findByPersonalIdAndMesReferencia(17L, periodo.atDay(1))).thenReturn(Optional.empty());

        service.sincronizar(personal, periodo);

        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacoes).save(captor.capture());
        Notificacao salva = captor.getValue();
        assertEquals(personal, salva.getPersonal());
        assertEquals(TipoNotificacao.META_RECEITA_NAO_DEFINIDA, salva.getTipo());
        assertEquals("Defina sua meta de receita", salva.getTitulo());
        assertTrue(salva.getChaveUnica().contains("17:" + periodo));
    }

    @Test
    void atualizaNotificacaoExistenteQuandoCondicaoContinuaAbaixoDoRitmo() {
        UsuarioPersonal personal = personal(23L);
        YearMonth periodo = YearMonth.now();
        MetaReceitaAcoes meta = new MetaReceitaAcoes();
        meta.setValorMeta(new BigDecimal("100.00"));
        when(metas.findByPersonalIdAndMesReferencia(23L, periodo.atDay(1))).thenReturn(Optional.of(meta));
        when(vinculos.findByInteracaoPersonalIdOrderByDataVinculoDesc(23L)).thenReturn(List.of());
        // Sem receita, a condição correta é abaixo do ritmo; devolvemos a notificação já existente para validar atualização.
        String chave = "META_RECEITA:23:" + periodo + ":META_RECEITA_ABAIXO_RITMO";
        Notificacao existente = new Notificacao();
        existente.setPersonal(personal);
        existente.setTipo(TipoNotificacao.META_RECEITA_ABAIXO_RITMO);
        existente.setChaveUnica(chave);
        existente.setTitulo("Mensagem antiga");
        existente.setMensagem("Conteúdo desatualizado");
        when(notificacoes.findByChaveUnica(chave)).thenReturn(Optional.of(existente));

        service.sincronizar(personal, periodo);

        assertNotEquals("Mensagem antiga", existente.getTitulo());
        assertTrue(existente.getTitulo().contains("ritmo"));
        verify(notificacoes, atLeastOnce()).save(existente);
    }

    @Test
    void naoCriaNotificacaoDeMetaNaoDefinidaParaMesPassado() {
        UsuarioPersonal personal = personal(31L);
        YearMonth passado = YearMonth.now().minusMonths(1);
        when(metas.findByPersonalIdAndMesReferencia(31L, passado.atDay(1))).thenReturn(Optional.empty());

        service.sincronizar(personal, passado);

        verify(notificacoes, never()).save(argThat(n -> n.getTipo() == TipoNotificacao.META_RECEITA_NAO_DEFINIDA));
    }

    private UsuarioPersonal personal(Long id) {
        UsuarioPersonal personal = new UsuarioPersonal();
        personal.setId(id);
        personal.setNome("Personal de teste");
        personal.setEmail("teste" + id + "@example.com");
        return personal;
    }
}
