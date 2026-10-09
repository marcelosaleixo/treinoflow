package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.ContaReceber;
import com.marceloaleixo.treinoflow.dto.ReceitaAlunoFinanceiraView;
import com.marceloaleixo.treinoflow.enums.StatusContaReceber;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContaReceberRepository extends JpaRepository<ContaReceber, Long> {
    Optional<ContaReceber> findByIdAndPersonalId(Long id, Long personalId);

    boolean existsByPlanoMensalidadeIdAndDataVencimento(Long planoMensalidadeId, LocalDate dataVencimento);

    @EntityGraph(attributePaths = {"aluno", "pagamentos"})
    List<ContaReceber> findTop12ByPersonalIdOrderByDataVencimentoAsc(Long personalId);

    @EntityGraph(attributePaths = {"aluno", "pagamentos"})
    List<ContaReceber> findByPersonalIdOrderByDataVencimentoAsc(Long personalId);


    @EntityGraph(attributePaths = {"aluno", "pagamentos"})
    @Query("select c from ContaReceber c where c.personal.id = :personalId and c.status in :status and c.dataVencimento <= :limite order by c.dataVencimento asc, c.id asc")
    List<ContaReceber> buscarParaCobranca(@Param("personalId") Long personalId,
                                           @Param("status") List<StatusContaReceber> status,
                                           @Param("limite") LocalDate limite);

    @EntityGraph(attributePaths = {"aluno", "pagamentos"})
    @Query("select c from ContaReceber c where c.id = :id and c.personal.id = :personalId")
    Optional<ContaReceber> buscarPorIdComAluno(@Param("id") Long id, @Param("personalId") Long personalId);


    @Query("select new com.marceloaleixo.treinoflow.dto.ReceitaAlunoFinanceiraView(a.id, a.nome, " +
           "sum(case when c.status = com.marceloaleixo.treinoflow.enums.StatusContaReceber.PAGA and c.dataPagamento between :inicio and :fim then c.valor else (c.valor - c.valor) end), " +
           "sum(case when c.status = com.marceloaleixo.treinoflow.enums.StatusContaReceber.PENDENTE then c.valor else (c.valor - c.valor) end), " +
           "sum(case when c.status = com.marceloaleixo.treinoflow.enums.StatusContaReceber.ATRASADA then c.valor else (c.valor - c.valor) end)) " +
           "from ContaReceber c join c.aluno a where c.personal.id = :personalId " +
           "group by a.id, a.nome order by sum(case when c.status = com.marceloaleixo.treinoflow.enums.StatusContaReceber.PAGA and c.dataPagamento between :inicio and :fim then c.valor else (c.valor - c.valor) end) desc, a.nome asc")
    List<ReceitaAlunoFinanceiraView> resumoFinanceiroPorAluno(@Param("personalId") Long personalId,
                                                               @Param("inicio") LocalDate inicio,
                                                               @Param("fim") LocalDate fim);

    @Query("select coalesce(sum(c.valor),0) from ContaReceber c where c.personal.id = :personalId and c.status = :status")
    BigDecimal somarPorStatus(@Param("personalId") Long personalId, @Param("status") StatusContaReceber status);

    @Query("select coalesce(sum(c.valor),0) from ContaReceber c where c.personal.id = :personalId and c.status = :status and c.dataPagamento between :inicio and :fim")
    BigDecimal somarPagasNoPeriodo(@Param("personalId") Long personalId, @Param("status") StatusContaReceber status,
                                   @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("select coalesce(sum(c.valor),0) from ContaReceber c where c.personal.id = :personalId and c.status = :status and c.dataVencimento between :inicio and :fim")
    BigDecimal somarVencimentosNoPeriodo(@Param("personalId") Long personalId, @Param("status") StatusContaReceber status,
                                         @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("select count(c) from ContaReceber c where c.personal.id = :personalId and c.status = :status")
    long contarPorStatus(@Param("personalId") Long personalId, @Param("status") StatusContaReceber status);

    @Query("select c from ContaReceber c left join fetch c.aluno where c.personal.id = :personalId and c.status = :status and c.dataVencimento < :hoje order by c.dataVencimento asc")
    List<ContaReceber> vencidas(@Param("personalId") Long personalId, @Param("status") StatusContaReceber status, @Param("hoje") LocalDate hoje);
}
