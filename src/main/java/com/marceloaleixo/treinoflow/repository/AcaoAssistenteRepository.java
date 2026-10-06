package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.AcaoAssistente;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AcaoAssistenteRepository extends JpaRepository<AcaoAssistente, Long> {
    @Query("select a from AcaoAssistente a join fetch a.aluno where a.personal.id = :personalId and a.executadaEm >= :inicio order by a.executadaEm desc")
    List<AcaoAssistente> buscarDesde(@Param("personalId") Long personalId, @Param("inicio") LocalDateTime inicio);

    @Query("select count(a) from AcaoAssistente a where a.personal.id = :personalId and a.executadaEm >= :inicio")
    long countDesde(@Param("personalId") Long personalId, @Param("inicio") LocalDateTime inicio);

    @Query("select count(a) from AcaoAssistente a where a.personal.id = :personalId and a.executadaEm >= :inicio and a.resultado = :resultado")
    long countPorResultadoDesde(@Param("personalId") Long personalId, @Param("inicio") LocalDateTime inicio, @Param("resultado") ResultadoCrm resultado);

    @Query("select count(a) from AcaoAssistente a where a.personal.id = :personalId and a.automatica = true and a.executadaEm >= :inicio")
    long countAutomaticasDesde(@Param("personalId") Long personalId, @Param("inicio") LocalDateTime inicio);

    @Query("select count(a) > 0 from AcaoAssistente a where a.personal.id = :personalId and a.aluno.id = :alunoId and a.automatica = true and a.executadaEm >= :inicio")
    boolean existeAutomaticaDesde(@Param("personalId") Long personalId, @Param("alunoId") Long alunoId, @Param("inicio") LocalDateTime inicio);

    @Query("select a from AcaoAssistente a join fetch a.aluno where a.personal.id = :personalId and a.automatica = true and a.jornadaStatus = 'AGUARDANDO_RESPOSTA' and a.proximaAcaoEm is not null and a.proximaAcaoEm <= :agora order by a.proximaAcaoEm asc")
    List<AcaoAssistente> buscarJornadasPendentes(@Param("personalId") Long personalId, @Param("agora") LocalDateTime agora);

    @Query("select count(a) from AcaoAssistente a where a.personal.id = :personalId and a.automatica = true and a.jornadaStatus = 'AGUARDANDO_RESPOSTA'")
    long countJornadasAtivas(@Param("personalId") Long personalId);

    @Query("select count(a) from AcaoAssistente a where a.personal.id = :personalId and a.automatica = true and a.jornadaStatus = :status")
    long countJornadasPorStatus(@Param("personalId") Long personalId, @Param("status") String status);

    @Query("select a from AcaoAssistente a join fetch a.aluno where a.personal.id = :personalId and a.executadaEm >= :inicio and a.executadaEm <= :fim and a.automatica = :automatica order by a.executadaEm desc")
    List<AcaoAssistente> buscarPeriodo(@Param("personalId") Long personalId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim, @Param("automatica") boolean automatica);

    @Query("select a from AcaoAssistente a join fetch a.aluno where a.id = :id and a.personal.id = :personalId")
    Optional<AcaoAssistente> buscarPorIdDoPersonal(@Param("id") Long id, @Param("personalId") Long personalId);
}
