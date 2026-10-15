package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.IncidenteOperacionalNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidenteOperacionalNotificacaoRepository extends JpaRepository<IncidenteOperacionalNotificacao, String> {
    List<IncidenteOperacionalNotificacao> findTop50ByOrderByOcorridoEmDesc();
    List<IncidenteOperacionalNotificacao> findByCodigoAndResolvidoFalse(String codigo);
    long countByResolvidoFalse();
    Optional<IncidenteOperacionalNotificacao> findByIdAndResolvidoFalse(String id);
}
