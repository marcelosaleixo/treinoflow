package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.DesafioPerformance;
import com.marceloaleixo.treinoflow.enums.TipoDesafioPerformance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DesafioPerformanceRepository extends JpaRepository<DesafioPerformance, Long> {
    List<DesafioPerformance> findByPersonalIdAndMesReferenciaOrderByIdAsc(Long personalId, LocalDate mesReferencia);
    Optional<DesafioPerformance> findByPersonalIdAndMesReferenciaAndTipo(Long personalId, LocalDate mesReferencia,
                                                                          TipoDesafioPerformance tipo);
}
