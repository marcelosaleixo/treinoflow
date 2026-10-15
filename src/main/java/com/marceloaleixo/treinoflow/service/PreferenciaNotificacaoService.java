package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.PreferenciaNotificacao;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.TipoNotificacao;
import com.marceloaleixo.treinoflow.repository.PreferenciaNotificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PreferenciaNotificacaoService {
    private final PreferenciaNotificacaoRepository repository;
    private final PreferenciaNotificacaoAuditoriaService auditoria;

    public PreferenciaNotificacaoService(PreferenciaNotificacaoRepository repository,
                                         PreferenciaNotificacaoAuditoriaService auditoria) {
        this.repository = repository;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public Map<TipoNotificacao, Boolean> listar(UsuarioPersonal personal) {
        Map<TipoNotificacao, Boolean> resultado = new EnumMap<>(TipoNotificacao.class);
        for (TipoNotificacao tipo : TipoNotificacao.values()) resultado.put(tipo, true);
        for (PreferenciaNotificacao preferencia : repository.findByPersonalId(personal.getId()))
            resultado.put(preferencia.getTipo(), preferencia.isTempoRealAtivo());
        return resultado;
    }

    @Transactional
    public void salvar(UsuarioPersonal personal, Set<TipoNotificacao> tiposAtivos) {
        Set<TipoNotificacao> ativos = tiposAtivos == null ? Set.of() : tiposAtivos;
        for (TipoNotificacao tipo : TipoNotificacao.values()) {
            boolean ativo = ativos.contains(tipo);
            PreferenciaNotificacao existente = repository.findByPersonalIdAndTipo(personal.getId(), tipo).orElse(null);
            boolean valorAnterior = existente == null || existente.isTempoRealAtivo();
            PreferenciaNotificacao preferencia = existente == null
                    ? new PreferenciaNotificacao(personal, tipo, ativo) : existente;
            if (valorAnterior != ativo) {
                auditoria.registrar(personal, tipo, valorAnterior, ativo);
            }
            preferencia.setTempoRealAtivo(ativo);
            repository.save(preferencia);
        }
    }

    @Transactional(readOnly = true)
    public boolean receberEmTempoReal(Long personalId, TipoNotificacao tipo) {
        return repository.findByPersonalIdAndTipo(personalId, tipo)
                .map(PreferenciaNotificacao::isTempoRealAtivo)
                .orElse(true); // comportamento anterior preservado para usuários sem preferências gravadas
    }
}
