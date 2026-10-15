package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.PreferenciaNotificacaoAuditoria;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.TipoNotificacao;
import com.marceloaleixo.treinoflow.repository.PreferenciaNotificacaoAuditoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreferenciaNotificacaoAuditoriaService {
    private final PreferenciaNotificacaoAuditoriaRepository repository;

    public PreferenciaNotificacaoAuditoriaService(PreferenciaNotificacaoAuditoriaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void registrar(UsuarioPersonal personal, TipoNotificacao tipo, boolean anterior, boolean novo) {
        if (anterior == novo) return;
        repository.save(new PreferenciaNotificacaoAuditoria(personal, tipo, anterior, novo));
    }

    @Transactional(readOnly = true)
    public Page<PreferenciaNotificacaoAuditoria> listarDoPersonal(Long personalId, int pagina) {
        int paginaSegura = Math.max(0, pagina);
        return repository.findByPersonalIdOrderByAlteradoEmDescIdDesc(personalId,
                PageRequest.of(paginaSegura, 25));
    }
}
