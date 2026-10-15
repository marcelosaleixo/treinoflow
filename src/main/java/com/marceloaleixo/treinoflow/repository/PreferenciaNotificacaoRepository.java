package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.PreferenciaNotificacao;
import com.marceloaleixo.treinoflow.enums.TipoNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PreferenciaNotificacaoRepository extends JpaRepository<PreferenciaNotificacao, Long> {
    List<PreferenciaNotificacao> findByPersonalId(Long personalId);
    Optional<PreferenciaNotificacao> findByPersonalIdAndTipo(Long personalId, TipoNotificacao tipo);
}
