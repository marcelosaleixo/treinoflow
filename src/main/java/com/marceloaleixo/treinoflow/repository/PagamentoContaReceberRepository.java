package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.PagamentoContaReceber;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PagamentoContaReceberRepository extends JpaRepository<PagamentoContaReceber, Long> {
    @EntityGraph(attributePaths = {"contaReceber", "contaReceber.aluno"})
    @Query("select p from PagamentoContaReceber p where p.contaReceber.personal.id = :personalId and p.dataPagamento between :inicio and :fim order by p.dataPagamento desc, p.id desc")
    List<PagamentoContaReceber> listarPeriodo(@Param("personalId") Long personalId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @EntityGraph(attributePaths = {"contaReceber", "contaReceber.aluno"})
    @Query("select p from PagamentoContaReceber p where p.id = :id and p.contaReceber.personal.id = :personalId")
    Optional<PagamentoContaReceber> buscarDoPersonal(@Param("id") Long id, @Param("personalId") Long personalId);
}
