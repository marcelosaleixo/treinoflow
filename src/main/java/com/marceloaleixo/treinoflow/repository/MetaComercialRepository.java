package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.MetaComercial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface MetaComercialRepository extends JpaRepository<MetaComercial, Long> {
    Optional<MetaComercial> findByPersonalIdAndMesReferencia(Long personalId, LocalDate mesReferencia);
}
