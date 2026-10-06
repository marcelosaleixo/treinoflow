package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.AutomacaoRetencaoConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AutomacaoRetencaoConfigRepository extends JpaRepository<AutomacaoRetencaoConfig, Long> {
    Optional<AutomacaoRetencaoConfig> findByPersonalId(Long personalId);
    java.util.List<AutomacaoRetencaoConfig> findByAtivaTrue();
}
