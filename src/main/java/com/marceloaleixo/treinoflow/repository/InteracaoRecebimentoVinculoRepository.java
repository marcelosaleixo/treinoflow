package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.InteracaoRecebimentoVinculo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface InteracaoRecebimentoVinculoRepository extends JpaRepository<InteracaoRecebimentoVinculo, Long> {
    boolean existsByInteracaoIdAndPagamentoId(Long interacaoId, Long pagamentoId);

    boolean existsByPagamentoId(Long pagamentoId);

    java.util.Optional<InteracaoRecebimentoVinculo> findByIdAndInteracaoPersonalId(Long id, Long personalId);

    @EntityGraph(attributePaths = {"interacao", "interacao.aluno", "pagamento", "pagamento.contaReceber", "pagamento.contaReceber.aluno"})
    List<InteracaoRecebimentoVinculo> findByInteracaoPersonalIdOrderByDataVinculoDesc(Long personalId);

    @Query("select coalesce(sum(v.pagamento.valor), 0) from InteracaoRecebimentoVinculo v where v.interacao.personal.id = :personalId and v.interacao.id = :interacaoId")
    java.math.BigDecimal somarRecebimentosVinculados(@Param("personalId") Long personalId, @Param("interacaoId") Long interacaoId);
}
