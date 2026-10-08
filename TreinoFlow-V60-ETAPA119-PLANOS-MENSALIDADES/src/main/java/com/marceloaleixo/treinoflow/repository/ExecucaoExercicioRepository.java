package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.ExecucaoExercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExecucaoExercicioRepository extends JpaRepository<ExecucaoExercicio, Long> {
    Optional<ExecucaoExercicio> findByRegistroIdAndTreinoExercicioId(Long registroId, Long treinoExercicioId);

    List<ExecucaoExercicio> findByRegistroId(Long registroId);

    @Query("""
        SELECT e FROM ExecucaoExercicio e
        JOIN FETCH e.registro r
        JOIN FETCH e.treinoExercicio te
        JOIN FETCH te.exercicio ex
        WHERE r.aluno.id = :alunoId
        ORDER BY r.dataExecucao DESC, te.ordem ASC
    """)
    List<ExecucaoExercicio> buscarHistoricoDoAluno(@Param("alunoId") Long alunoId);

    @Query("""
        SELECT e
        FROM ExecucaoExercicio e
        JOIN FETCH e.registro r
        JOIN FETCH r.aluno a
        JOIN FETCH e.treinoExercicio te
        JOIN FETCH te.exercicio ex
        WHERE a.personal.id = :personalId
          AND r.dataExecucao >= :inicio
        ORDER BY r.dataExecucao DESC, te.ordem ASC
    """)
    List<ExecucaoExercicio> buscarDoPersonalDesde(@Param("personalId") Long personalId,
                                                   @Param("inicio") LocalDate inicio);
}
