package com.marceloaleixo.treinoflow.repository;
import com.marceloaleixo.treinoflow.entity.Assinatura;
import com.marceloaleixo.treinoflow.enums.StatusAssinatura;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate; import java.util.*;
public interface AssinaturaRepository extends JpaRepository<Assinatura,Long>{
 @EntityGraph(attributePaths={"personal","plano"}) Optional<Assinatura> findByPersonalId(Long personalId);
 @EntityGraph(attributePaths={"personal","plano"}) List<Assinatura> findAllByOrderByDataVencimentoAsc();
 long countByStatus(StatusAssinatura status);

 @EntityGraph(attributePaths={"personal","plano"})
 List<Assinatura> findByStatusInOrderByDataVencimentoAsc(List<StatusAssinatura> status);

 @Query("select coalesce(sum(a.plano.valorMensal), 0) from Assinatura a where a.status in :status")
 java.math.BigDecimal somarValorMensalPorStatus(@Param("status") List<StatusAssinatura> status);

 @Query("select count(a) from Assinatura a where a.plano.id = :planoId and a.status in :status")
 long countAtivasPorPlano(@Param("planoId") Long planoId, @Param("status") List<StatusAssinatura> status);
 @Query("select a from Assinatura a join fetch a.personal p join fetch a.plano where a.dataVencimento is not null and a.dataVencimento between :inicio and :fim order by a.dataVencimento") List<Assinatura> vencendoEntre(@Param("inicio") LocalDate inicio,@Param("fim") LocalDate fim);
}
