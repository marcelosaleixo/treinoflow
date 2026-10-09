package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.DespesaPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DespesaPersonalRepository extends JpaRepository<DespesaPersonal, Long> {
    List<DespesaPersonal> findByPersonalIdAndDataDespesaBetweenOrderByDataDespesaDescIdDesc(Long personalId, LocalDate inicio, LocalDate fim);
    Optional<DespesaPersonal> findByIdAndPersonalId(Long id, Long personalId);

    @Query("select coalesce(sum(d.valor), 0) from DespesaPersonal d where d.personal.id = :personalId and d.dataDespesa between :inicio and :fim and d.paga = true")
    BigDecimal somarPagas(@Param("personalId") Long personalId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("select coalesce(sum(d.valor), 0) from DespesaPersonal d where d.personal.id = :personalId and d.dataDespesa between :inicio and :fim and d.paga = false")
    BigDecimal somarPendentes(@Param("personalId") Long personalId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
