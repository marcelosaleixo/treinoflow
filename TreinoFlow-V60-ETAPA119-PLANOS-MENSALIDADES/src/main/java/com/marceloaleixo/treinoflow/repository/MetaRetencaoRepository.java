package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.MetaRetencao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MetaRetencaoRepository extends JpaRepository<MetaRetencao, Long> {
    Optional<MetaRetencao> findByPersonalIdAndMesReferencia(Long personalId, LocalDate mesReferencia);
    List<MetaRetencao> findTop6ByPersonalIdOrderByMesReferenciaDesc(Long personalId);
}
