package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.ConciliacaoFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ConciliacaoFinanceiraRepository extends JpaRepository<ConciliacaoFinanceira, Long> {
    Optional<ConciliacaoFinanceira> findByPagamentoIdAndPersonalId(Long pagamentoId, Long personalId);
    List<ConciliacaoFinanceira> findByPersonalIdAndPagamentoIdIn(Long personalId, Collection<Long> pagamentoIds);
}
