package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.Cobranca;
import com.marceloaleixo.treinoflow.enums.StatusCobranca;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CobrancaRepository extends JpaRepository<Cobranca, Long> {
    @EntityGraph(attributePaths = {"assinatura", "assinatura.personal", "assinatura.plano"})
    List<Cobranca> findAllByOrderByDataVencimentoAsc();

    @EntityGraph(attributePaths = {"assinatura", "assinatura.personal", "assinatura.plano"})
    List<Cobranca> findByAssinaturaPersonalIdOrderByDataVencimentoDesc(Long personalId);

    Optional<Cobranca> findByAssinaturaIdAndCompetencia(Long assinaturaId, LocalDate competencia);

    @EntityGraph(attributePaths = {"assinatura", "assinatura.personal", "assinatura.plano"})
    List<Cobranca> findByStatusOrderByDataVencimentoAsc(StatusCobranca status);

    long countByStatus(StatusCobranca status);

    @Query("select coalesce(sum(c.valor), 0) from Cobranca c where c.status = :status")
    BigDecimal somarPorStatus(@Param("status") StatusCobranca status);

    @Query("select c from Cobranca c join fetch c.assinatura a join fetch a.personal p join fetch a.plano where c.dataVencimento < :hoje and c.status = :status order by c.dataVencimento")
    List<Cobranca> vencidasNaoPagas(@Param("hoje") LocalDate hoje, @Param("status") StatusCobranca status);
}
