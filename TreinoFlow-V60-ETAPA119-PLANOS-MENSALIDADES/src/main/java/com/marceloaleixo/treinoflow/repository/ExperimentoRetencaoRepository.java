package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.ExperimentoRetencao;
import com.marceloaleixo.treinoflow.enums.TipoAcaoAssistente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ExperimentoRetencaoRepository extends JpaRepository<ExperimentoRetencao, Long> {
    Optional<ExperimentoRetencao> findFirstByPersonalIdAndFaixaRiscoAndTipoAcaoAndStatusOrderByCriadoEmDesc(Long personalId, String faixaRisco, TipoAcaoAssistente tipoAcao, String status);
    List<ExperimentoRetencao> findByPersonalIdOrderByCriadoEmDesc(Long personalId);
}
