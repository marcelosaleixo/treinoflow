package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    @Query("SELECT a FROM Agendamento a JOIN FETCH a.aluno " +
            "WHERE a.personal.id = :personalId " +
            "AND a.inicio >= :inicio AND a.inicio < :fim " +
            "ORDER BY a.inicio ASC")
    List<Agendamento> buscarDoDia(@Param("personalId") Long personalId,
                                   @Param("inicio") LocalDateTime inicio,
                                   @Param("fim") LocalDateTime fim);

    Optional<Agendamento> findByIdAndPersonalId(Long id, Long personalId);

    @Query("SELECT a FROM Agendamento a JOIN FETCH a.aluno al JOIN FETCH a.personal p " +
            "WHERE a.status IN ('AGENDADO','CONFIRMADO') " +
            "AND a.inicio >= :inicio AND a.inicio <= :fim " +
            "ORDER BY a.inicio ASC")
    List<Agendamento> buscarProximosParaLembrete(@Param("inicio") LocalDateTime inicio,
                                                  @Param("fim") LocalDateTime fim);

    @Query("SELECT a FROM Agendamento a JOIN FETCH a.aluno al JOIN FETCH a.personal p " +
            "WHERE a.id = :id AND a.aluno.id = :alunoId")
    Optional<Agendamento> findByIdAndAlunoId(@Param("id") Long id, @Param("alunoId") Long alunoId);

    @Query("SELECT a FROM Agendamento a " +
            "WHERE a.aluno.id = :alunoId AND a.inicio >= :agora " +
            "AND a.inicio < :limite AND a.status IN ('AGENDADO','CONFIRMADO') " +
            "ORDER BY a.inicio ASC")
    List<Agendamento> buscarProximosDoAluno(@Param("alunoId") Long alunoId,
                                             @Param("agora") LocalDateTime agora,
                                             @Param("limite") LocalDateTime limite);

    @Query("SELECT a FROM Agendamento a JOIN FETCH a.aluno al " +
            "WHERE a.personal.id = :personalId " +
            "AND a.inicio >= :inicio AND a.inicio < :fim " +
            "ORDER BY a.inicio DESC")
    List<Agendamento> buscarDoPeriodoComAluno(@Param("personalId") Long personalId,
                                               @Param("inicio") LocalDateTime inicio,
                                               @Param("fim") LocalDateTime fim);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Agendamento a " +
            "WHERE a.personal.id = :personalId " +
            "AND a.status <> 'CANCELADO' " +
            "AND a.inicio < :fim AND a.fim > :inicio " +
            "AND (:id IS NULL OR a.id <> :id)")
    boolean existeConflito(@Param("personalId") Long personalId,
                           @Param("inicio") LocalDateTime inicio,
                           @Param("fim") LocalDateTime fim,
                           @Param("id") Long id);
}
