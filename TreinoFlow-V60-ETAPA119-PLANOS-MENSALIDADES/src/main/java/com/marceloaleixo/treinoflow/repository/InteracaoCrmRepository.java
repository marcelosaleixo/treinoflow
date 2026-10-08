package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.InteracaoCrm;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface InteracaoCrmRepository extends JpaRepository<InteracaoCrm, Long> {
    @EntityGraph(attributePaths = {"aluno"})
    List<InteracaoCrm> findByPersonalIdOrderByDataContatoDesc(Long personalId);

    @EntityGraph(attributePaths = {"aluno"})
    List<InteracaoCrm> findByPersonalIdAndAlunoIdOrderByDataContatoDesc(Long personalId, Long alunoId);

    @EntityGraph(attributePaths = {"aluno"})
    Page<InteracaoCrm> findByPersonalIdOrderByDataContatoDesc(Long personalId, Pageable pageable);

    long countByPersonalId(Long personalId);
    java.util.Optional<InteracaoCrm> findByIdAndPersonalId(Long id, Long personalId);
    long countByPersonalIdAndResultado(Long personalId, ResultadoCrm resultado);

    @Query("select count(distinct i.aluno.id) from InteracaoCrm i where i.personal.id = :personalId and i.resultado = :resultado")
    long countAlunosDistinctPorResultado(@Param("personalId") Long personalId, @Param("resultado") ResultadoCrm resultado);

    @Query("select count(i) from InteracaoCrm i where i.personal.id = :personalId and i.dataProximaAcao is not null and i.dataProximaAcao <= :hoje and i.resultado = :resultado")
    long countAcoesPendentes(@Param("personalId") Long personalId, @Param("hoje") LocalDate hoje, @Param("resultado") ResultadoCrm resultado);

    @Query("select count(distinct i.aluno.id) from InteracaoCrm i where i.personal.id = :personalId and i.dataContato >= :inicio")
    long countAlunosComContatoDesde(@Param("personalId") Long personalId, @Param("inicio") LocalDateTime inicio);

    @EntityGraph(attributePaths = {"aluno"})
    @Query("select i from InteracaoCrm i where i.personal.id = :personalId and i.dataProximaAcao is not null and i.dataProximaAcao < :hoje and i.resultado = :resultado order by i.dataProximaAcao asc, i.dataContato desc")
    List<InteracaoCrm> buscarAcoesVencidas(@Param("personalId") Long personalId, @Param("hoje") LocalDate hoje, @Param("resultado") ResultadoCrm resultado);

    @EntityGraph(attributePaths = {"aluno"})
    @Query("select i from InteracaoCrm i where i.personal.id = :personalId and i.dataProximaAcao is not null and i.dataProximaAcao <= :limite and i.resultado = :resultado order by i.dataProximaAcao asc, i.dataContato desc")
    List<InteracaoCrm> buscarAcoesAtePorResultado(@Param("personalId") Long personalId, @Param("limite") LocalDate limite, @Param("resultado") ResultadoCrm resultado);

    @EntityGraph(attributePaths = {"aluno"})
    @Query("select i from InteracaoCrm i where i.personal.id = :personalId and i.resultado = :resultado and i.dataContato >= :inicio order by i.dataContato desc")
    List<InteracaoCrm> buscarResultadosDesde(@Param("personalId") Long personalId, @Param("resultado") ResultadoCrm resultado, @Param("inicio") LocalDateTime inicio);

    @Query("select count(i) > 0 from InteracaoCrm i where i.personal.id = :personalId and i.aluno.id = :alunoId and i.resultado = :resultado and i.dataProximaAcao is not null")
    boolean existeFollowUpPendente(@Param("personalId") Long personalId, @Param("alunoId") Long alunoId, @Param("resultado") ResultadoCrm resultado);

    @Query("select count(i) > 0 from InteracaoCrm i where i.personal.id = :personalId and i.aluno.id = :alunoId and i.tipo = com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm.COBRANCA and i.assunto = :assunto and i.resultado = com.marceloaleixo.treinoflow.enums.ResultadoCrm.EM_ACOMPANHAMENTO")
    boolean existeCobrancaPendente(@Param("personalId") Long personalId, @Param("alunoId") Long alunoId, @Param("assunto") String assunto);

    @EntityGraph(attributePaths = {"aluno"})
    @Query("select i from InteracaoCrm i where i.personal.id = :personalId and i.tipo = com.marceloaleixo.treinoflow.enums.TipoInteracaoCrm.RETENCAO and i.dataContato >= :inicio order by i.dataContato desc")
    List<InteracaoCrm> buscarRetencoesDesde(@Param("personalId") Long personalId, @Param("inicio") LocalDateTime inicio);

    @Query("select count(i) > 0 from InteracaoCrm i where i.personal.id = :personalId and i.aluno.id = :alunoId and i.dataContato >= :inicio")
    boolean existeContatoDesde(@Param("personalId") Long personalId, @Param("alunoId") Long alunoId, @Param("inicio") LocalDateTime inicio);


    @Query("select max(i.dataContato) from InteracaoCrm i where i.personal.id = :personalId and i.aluno.id = :alunoId")
    LocalDateTime ultimaInteracao(@Param("personalId") Long personalId, @Param("alunoId") Long alunoId);

    @EntityGraph(attributePaths = {"aluno"})
    @Query("select i from InteracaoCrm i where i.personal.id = :personalId and i.aluno.id = :alunoId and i.dataContato > :inicio order by i.dataContato desc")
    List<InteracaoCrm> buscarDepois(@Param("personalId") Long personalId, @Param("alunoId") Long alunoId, @Param("inicio") LocalDateTime inicio);

    @Query("select count(a) from Aluno a where a.personal.id = :personalId and a.status = 'ATIVO' and not exists (select i.id from InteracaoCrm i where i.aluno.id = a.id and i.dataContato >= :inicio)")
    long countAlunosSemContatoDesde(@Param("personalId") Long personalId, @Param("inicio") LocalDateTime inicio);
}
