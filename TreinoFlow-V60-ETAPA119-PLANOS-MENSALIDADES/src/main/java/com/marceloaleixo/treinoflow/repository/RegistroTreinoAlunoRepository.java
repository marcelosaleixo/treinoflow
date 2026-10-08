package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.RegistroTreinoAluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroTreinoAlunoRepository extends JpaRepository<RegistroTreinoAluno, Long> {
    Optional<RegistroTreinoAluno> findByTreinoIdAndDataExecucao(Long treinoId, LocalDate dataExecucao);

    Optional<RegistroTreinoAluno> findTopByTreinoIdOrderByDataExecucaoDesc(Long treinoId);

    List<RegistroTreinoAluno> findTop20ByAlunoIdOrderByDataRegistroDesc(Long alunoId);

    @Query("SELECT r FROM RegistroTreinoAluno r JOIN FETCH r.aluno a JOIN FETCH r.treino t WHERE a.personal.id = :personalId ORDER BY r.dataRegistro DESC")
    List<RegistroTreinoAluno> findTop30ByAlunoPersonalIdOrderByDataRegistroDesc(@Param("personalId") Long personalId);

    @Query("SELECT r FROM RegistroTreinoAluno r JOIN FETCH r.treino t WHERE r.aluno.id = :alunoId ORDER BY r.dataExecucao DESC, r.dataRegistro DESC")
    List<RegistroTreinoAluno> buscarHistorico(@Param("alunoId") Long alunoId);

    @Query("SELECT COUNT(r) FROM RegistroTreinoAluno r WHERE r.aluno.id = :alunoId AND r.dataExecucao = :data AND r.concluido = true")
    long contarConcluidosNoDia(@Param("alunoId") Long alunoId, @Param("data") LocalDate data);

    @Query("""
        SELECT r
        FROM RegistroTreinoAluno r
        JOIN FETCH r.aluno a
        JOIN FETCH r.treino t
        WHERE a.personal.id = :personalId
          AND r.concluido = true
          AND r.dataExecucao >= :inicio
        ORDER BY r.dataExecucao DESC, r.dataRegistro DESC
    """)
    List<RegistroTreinoAluno> buscarConcluidosDoPersonalDesde(@Param("personalId") Long personalId,
                                                               @Param("inicio") LocalDate inicio);

    @Query("SELECT COUNT(r) FROM RegistroTreinoAluno r WHERE r.aluno.personal.id = :personalId AND r.concluido = true AND r.dataExecucao >= :inicio AND r.dataExecucao < :fim")
    long countTreinosConcluidosNoPeriodo(@Param("personalId") Long personalId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
    @Query("SELECT COUNT(DISTINCT r.aluno.id) FROM RegistroTreinoAluno r WHERE r.aluno.personal.id = :personalId AND r.concluido = true AND r.dataExecucao >= :inicio AND r.dataExecucao < :fim")
    long countAlunosComTreinoNoPeriodo(@Param("personalId") Long personalId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
